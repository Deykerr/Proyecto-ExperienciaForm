import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
// Asegúrate de que la ruta apunte a donde guardaste el interceptor que hicimos al principio
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    // Habilitamos HttpClient y le inyectamos nuestro interceptor de seguridad
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]))
  ]
};