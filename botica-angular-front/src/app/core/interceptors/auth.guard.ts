import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isLoggedIn()) {
        router.navigate(['/login']);
        return false;
    }

    const usuario = authService.currentUser();
    const allowedRoles = route.data?.['roles'] as Array<string>;

    if (allowedRoles && usuario) {
        if (!allowedRoles.includes(usuario.rol)) {
            // Si es almacenero y trata de entrar a POS, mandarlo a compras
            if (usuario.rol === 'ALMACENERO') {
                router.navigate(['/dashboard/compras']);
            } else if (usuario.rol === 'CAJERO') {
                router.navigate(['/dashboard/pos']);
            } else {
                router.navigate(['/dashboard']);
            }
            return false;
        }
    }

    // Ruta base redirige inteligentemente según rol si está vacía
    if (state.url === '/dashboard' || state.url === '/dashboard/') {
        if (usuario?.rol === 'ALMACENERO') {
            router.navigate(['/dashboard/compras']);
            return false;
        } else if (usuario?.rol === 'CAJERO') {
            router.navigate(['/dashboard/pos']);
            return false;
        }
    }

    return true;
};
