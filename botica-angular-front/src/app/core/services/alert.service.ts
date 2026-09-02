import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

@Injectable({
    providedIn: 'root'
})
export class AlertService {

    success(title: string, message: string = '') {
        Swal.fire({
            icon: 'success',
            title: title,
            text: message,
            confirmButtonColor: '#28a745',
            timer: 2500,
            showConfirmButton: false
        });
    }

    error(title: string, message: string = '') {
        Swal.fire({
            icon: 'error',
            title: title,
            text: message,
            confirmButtonColor: '#dc3545'
        });
    }

    warning(title: string, message: string = '') {
        Swal.fire({
            icon: 'warning',
            title: title,
            text: message,
            confirmButtonColor: '#ffc107'
        });
    }

    // Ideal para confirmar antes de eliminar algo o cerrar caja
    confirm(title: string, message: string = ''): Promise<any> {
        return Swal.fire({
            title: title,
            text: message,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#007bff',
            cancelButtonColor: '#dc3545',
            confirmButtonText: 'Sí, continuar',
            cancelButtonText: 'Cancelar'
        });
    }
}