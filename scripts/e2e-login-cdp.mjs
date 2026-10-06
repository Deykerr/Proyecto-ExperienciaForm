import { spawn } from 'node:child_process';
import { mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { createServer } from 'node:net';

const chromeBin = process.env.CHROME_BIN;
const baseUrl = process.env.BOTICA_E2E_URL ?? 'http://127.0.0.1:8081';
const username = process.env.BOTICA_E2E_USERNAME ?? 'admin';
const password = process.env.BOTICA_E2E_PASSWORD;

if (!chromeBin || !password) {
  throw new Error('CHROME_BIN y BOTICA_E2E_PASSWORD son obligatorios.');
}

const port = await obtenerPuertoLibre();
const profileDir = mkdtempSync(join(tmpdir(), 'botica-cdp-'));
const chrome = spawn(chromeBin, [
  '--headless=new',
  '--disable-gpu',
  '--no-first-run',
  '--no-default-browser-check',
  `--remote-debugging-port=${port}`,
  `--user-data-dir=${profileDir}`,
  'about:blank'
], { stdio: 'ignore' });

try {
  const target = await esperarTarget(port);
  const ws = new WebSocket(target.webSocketDebuggerUrl);
  await new Promise((resolve, reject) => {
    ws.addEventListener('open', resolve, { once: true });
    ws.addEventListener('error', reject, { once: true });
  });

  let sequence = 0;
  const pending = new Map();
  let loginRequestId;
  let loginHeaders = {};
  let loginStatus = 0;
  const extraHeadersByRequest = new Map();

  ws.addEventListener('message', event => {
    const message = JSON.parse(String(event.data));
    if (message.id && pending.has(message.id)) {
      const { resolve, reject } = pending.get(message.id);
      pending.delete(message.id);
      return message.error ? reject(new Error(message.error.message)) : resolve(message.result);
    }

    if (message.method === 'Network.requestWillBeSent'
        && message.params.request.url.endsWith('/api/auth/login')) {
      loginRequestId = message.params.requestId;
      loginHeaders = normalizarHeaders(message.params.request.headers);
    }
    if (message.method === 'Network.requestWillBeSentExtraInfo') {
      extraHeadersByRequest.set(
        message.params.requestId,
        normalizarHeaders(message.params.headers)
      );
    }
    if (message.method === 'Network.responseReceived'
        && message.params.requestId === loginRequestId) {
      loginStatus = message.params.response.status;
    }
  });

  const enviar = (method, params = {}) => new Promise((resolve, reject) => {
    const id = ++sequence;
    pending.set(id, { resolve, reject });
    ws.send(JSON.stringify({ id, method, params }));
  });

  await enviar('Network.enable');
  await enviar('Page.enable');
  await enviar('Runtime.enable');
  await enviar('Page.navigate', { url: `${baseUrl}/login?e2e=${Date.now()}` });

  await esperar(async () => {
    const result = await evaluar(enviar,
      "document.readyState === 'complete' && !!document.querySelector('input[type=password]')");
    return result === true;
  }, 15000);

  await evaluar(enviar, `(() => {
    const inputs = document.querySelectorAll('input');
    const userInput = [...inputs].find(input => input.type !== 'password');
    const passwordInput = document.querySelector('input[type=password]');
    const setValue = (input, value) => {
      const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
      setter.call(input, value);
      input.dispatchEvent(new Event('input', { bubbles: true }));
      input.dispatchEvent(new Event('change', { bubbles: true }));
    };
    setValue(userInput, ${JSON.stringify(username)});
    setValue(passwordInput, ${JSON.stringify(password)});
    document.querySelector('form').requestSubmit();
    return true;
  })()`);

  await esperar(() => loginStatus !== 0, 15000);
  await new Promise(resolve => setTimeout(resolve, 1000));

  const cookies = (await enviar('Network.getAllCookies')).cookies;
  const currentPath = await evaluar(enviar, 'location.pathname');
  const visibleError = await evaluar(enviar,
    "document.body.innerText.includes('No se pudo validar la sesión segura')");
  const loginExtraHeaders = extraHeadersByRequest.get(loginRequestId) ?? {};
  const requestCookie = loginExtraHeaders.cookie ?? loginHeaders.cookie ?? '';
  const csrfHeader = loginHeaders['x-xsrf-token'] ?? loginExtraHeaders['x-xsrf-token'] ?? '';
  const csrfSession = await evaluar(enviar, "sessionStorage.getItem('csrf_token') ?? ''");
  const csrfCookies = requestCookie
    .split(';')
    .map(cookie => cookie.trim())
    .filter(cookie => cookie.startsWith('XSRF-TOKEN='))
    .map(cookie => cookie.substring('XSRF-TOKEN='.length));

  const report = {
    loginStatus,
    csrfHeaderPresent: Boolean(csrfHeader),
    csrfCookieSent: requestCookie.includes('XSRF-TOKEN='),
    csrfCookieStored: cookies.some(cookie => cookie.name === 'XSRF-TOKEN'),
    csrfCookieMetadata: cookies
      .filter(cookie => cookie.name === 'XSRF-TOKEN')
      .map(cookie => ({
        domain: cookie.domain,
        path: cookie.path,
        secure: cookie.secure,
        httpOnly: cookie.httpOnly,
        sameSite: cookie.sameSite,
        session: cookie.session
      })),
    csrfCookieCountSent: csrfCookies.length,
    headerMatchesSessionToken: Boolean(csrfHeader) && csrfHeader === csrfSession,
    headerMatchesAnyCookie: csrfCookies.some(cookie =>
      cookie === csrfHeader || decodeURIComponent(cookie) === csrfHeader),
    sessionCookieStored: cookies.some(cookie => cookie.name === 'BOTICA_SESSION'),
    currentPath,
    visibleError
  };

  process.stdout.write(`${JSON.stringify(report, null, 2)}\n`);
  ws.close();
  if (loginStatus !== 200 || !report.sessionCookieStored || currentPath === '/login') {
    process.exitCode = 1;
  }
} finally {
  chrome.kill();
  await new Promise(resolve => chrome.once('exit', resolve));
  await new Promise(resolve => setTimeout(resolve, 500));
  try {
    rmSync(profileDir, { recursive: true, force: true, maxRetries: 5, retryDelay: 200 });
  } catch {
    // Windows puede mantener archivos de Chrome bloqueados unos instantes.
  }
}

function normalizarHeaders(headers) {
  return Object.fromEntries(Object.entries(headers ?? {}).map(([key, value]) => [key.toLowerCase(), value]));
}

async function evaluar(enviar, expression) {
  const response = await enviar('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true });
  if (response.exceptionDetails) {
    throw new Error(response.exceptionDetails.text);
  }
  return response.result.value;
}

async function esperar(predicate, timeoutMs) {
  const limite = Date.now() + timeoutMs;
  while (Date.now() < limite) {
    if (await predicate()) return;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error(`Tiempo de espera agotado después de ${timeoutMs} ms.`);
}

async function esperarTarget(debugPort) {
  const limite = Date.now() + 10000;
  while (Date.now() < limite) {
    try {
      const response = await fetch(`http://127.0.0.1:${debugPort}/json/list`);
      const targets = await response.json();
      const page = targets.find(target => target.type === 'page');
      if (page) return page;
    } catch {
      // Chrome todavía está inicializando el puerto de depuración.
    }
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error('Chrome no habilitó el protocolo de depuración a tiempo.');
}

function obtenerPuertoLibre() {
  return new Promise((resolve, reject) => {
    const server = createServer();
    server.once('error', reject);
    server.listen(0, '127.0.0.1', () => {
      const { port: freePort } = server.address();
      server.close(error => error ? reject(error) : resolve(freePort));
    });
  });
}
