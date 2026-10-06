import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    sessionStorage.clear();
  });

  it('conserva el token CSRF explícito aunque exista otro token en la sesión', () => {
    sessionStorage.setItem('csrf_token', 'token-obsoleto');

    http.post('/api/auth/login', {}, {
      headers: { 'X-XSRF-TOKEN': 'token-reciente' }
    }).subscribe();

    const request = httpTesting.expectOne('/api/auth/login');
    expect(request.request.headers.get('X-XSRF-TOKEN')).toBe('token-reciente');
    expect(request.request.withCredentials).toBeTrue();
    request.flush({});
  });

  it('usa el token de sesión en peticiones mutables sin encabezado explícito', () => {
    sessionStorage.setItem('csrf_token', 'token-de-sesion');

    http.post('/api/ventas', {}).subscribe();

    const request = httpTesting.expectOne('/api/ventas');
    expect(request.request.headers.get('X-XSRF-TOKEN')).toBe('token-de-sesion');
    expect(request.request.withCredentials).toBeTrue();
    request.flush({});
  });
});
