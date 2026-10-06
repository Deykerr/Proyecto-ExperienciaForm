import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const metodosSeguros = ['GET', 'HEAD', 'OPTIONS', 'TRACE'];
  const csrfEnPeticion = req.headers.get('X-XSRF-TOKEN');
  const csrfToken = csrfEnPeticion
    ?? sessionStorage.getItem('csrf_token')
    ?? leerCookie('XSRF-TOKEN');
  let headers = req.headers;

  if (!metodosSeguros.includes(req.method.toUpperCase()) && csrfToken && !csrfEnPeticion) {
    headers = headers.set('X-XSRF-TOKEN', decodeURIComponent(csrfToken));
  }

  return next(req.clone({ withCredentials: true, headers }));
};

function leerCookie(nombre: string): string | null {
  const prefijo = `${nombre}=`;
  const cookie = document.cookie
    .split(';')
    .map(valor => valor.trim())
    .find(valor => valor.startsWith(prefijo));
  return cookie ? cookie.substring(prefijo.length) : null;
}
