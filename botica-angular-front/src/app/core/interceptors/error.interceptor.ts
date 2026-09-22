import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AlertService } from '../services/alert.service';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const alertService = inject(AlertService);
    const authService = inject(AuthService);
    const router = inject(Router);

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            let errorMessage = 'Ocurrió un error inesperado';

            if (error.error instanceof ErrorEvent) {
                // Error del lado del cliente o de red
                errorMessage = `Error: ${error.error.message}`;
                alertService.error('Error de Conexión', 'No se pudo conectar con el servidor.');
            } else {
                // Error devuelto por el backend (Spring Boot)
                switch (error.status) {
                    case 401:
                        // Token expirado o inválido
                        alertService.warning('Sesión Expirada', 'Por favor, vuelve a iniciar sesión.');
                        authService.logout(false);
                        router.navigate(['/login']);
                        break;
                    case 403:
                        alertService.error('Acceso Denegado', 'No tienes permisos para esta acción.');
                        break;
                    case 404:
                        alertService.warning('No Encontrado', 'El recurso solicitado no existe.');
                        break;
                    case 400:
                        // Manejar validaciones del backend
                        errorMessage = error.error?.message || 'Datos incorrectos.';
                        alertService.error('Error de Validación', errorMessage);
                        break;
                    case 500:
                        alertService.error('Error del Servidor', 'No se pudo completar la operación. Inténtalo nuevamente.');
                        break;
                    default:
                        alertService.error('Error', `Código: ${error.status}`);
                        break;
                }
            }

            return throwError(() => error);
        })
    );
};
