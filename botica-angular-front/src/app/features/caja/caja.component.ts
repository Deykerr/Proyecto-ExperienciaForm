import {
    Component,
    inject,
    OnInit,
    signal
} from '@angular/core';

import {
    FormBuilder,
    ReactiveFormsModule,
    Validators
} from '@angular/forms';

import { DatePipe } from '@angular/common';

import { CajaService } from '../../core/services/caja.service';
import { AuthService } from '../../core/services/auth.service';

import {
    CajaSesionDTO
} from '../../core/models';

@Component({
    selector: 'app-caja',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        DatePipe
    ],
    templateUrl: './caja.component.html',
    styleUrl: './caja.component.scss'
})
export class CajaComponent implements OnInit {

    private fb = inject(FormBuilder);
    private cajaService = inject(CajaService);
    private authService = inject(AuthService);

    // ==========================================
    // ESTADOS
    // ==========================================

    cajaActiva = signal<CajaSesionDTO | null>(null);

    isLoading = signal(true);

    mensajeError = signal('');

    // ==========================================
    // FORMULARIO APERTURA
    // ==========================================

    formApertura = this.fb.nonNullable.group({
        montoInicial: [
            0,
            [
                Validators.required,
                Validators.min(0)
            ]
        ]
    });

    // ==========================================
    // FORMULARIO CIERRE
    // ==========================================

    formCierre = this.fb.nonNullable.group({
        montoFinal: [
            0,
            [
                Validators.required,
                Validators.min(0)
            ]
        ]
    });

    // ==========================================
    // INICIO
    // ==========================================

    ngOnInit(): void {
        this.verificarCajaActiva();
    }

    // ==========================================
    // OBTENER ID DEL USUARIO
    // ==========================================

    private obtenerIdUsuario(): number | null {

        const usuario = this.authService.currentUser();

        if (!usuario || !usuario.idUsuario) {
            return null;
        }

        return usuario.idUsuario;
    }

    // ==========================================
    // VERIFICAR CAJA ACTIVA
    // ==========================================

    verificarCajaActiva(): void {
        this.isLoading.set(true);
        this.mensajeError.set('');

        this.cajaService
            .obtenerCajaActiva()
            .subscribe({
                next: (caja) => {
                    this.cajaActiva.set(caja);
                    this.isLoading.set(false);
                },
                error: () => {
                    // Si no tiene caja abierta,
                    // simplemente mostramos la pantalla de apertura.
                    this.cajaActiva.set(null);
                    this.isLoading.set(false);
                }
            });
    }

    // ==========================================
    // ABRIR CAJA
    // ==========================================

    abrirCaja(): void {
        if (this.formApertura.invalid) {
            this.formApertura.markAllAsTouched();
            return;
        }

        this.isLoading.set(true);
        this.mensajeError.set('');

        const request = this.formApertura.getRawValue();

        this.cajaService
            .abrirCaja(request)
            .subscribe({
                next: (caja) => {
                    this.cajaActiva.set(caja);
                    this.formApertura.reset({
                        montoInicial: 0
                    });
                    this.isLoading.set(false);
                },
                error: (err) => {
                    console.error('Error al abrir caja:', err);
                    this.mensajeError.set(
                        err?.error?.message ||
                        'No se pudo abrir la caja.'
                    );
                    this.isLoading.set(false);
                }
            });
    }

    // ==========================================
    // CERRAR CAJA
    // ==========================================

    cerrarCaja(): void {

        const cajaActual = this.cajaActiva();

        if (!cajaActual) {
            return;
        }

        if (this.formCierre.invalid) {

            this.formCierre.markAllAsTouched();

            return;
        }

        this.isLoading.set(true);
        this.mensajeError.set('');

        const request = this.formCierre.getRawValue();

        this.cajaService
            .cerrarCaja(
                cajaActual.idCajaSesion,
                request
            )
            .subscribe({

                next: () => {

                    this.cajaActiva.set(null);

                    this.formCierre.reset({
                        montoFinal: 0
                    });

                    this.isLoading.set(false);
                },

                error: (err) => {

                    console.error('Error al cerrar caja:', err);

                    this.mensajeError.set(
                        err?.error?.message ||
                        'No se pudo cerrar la caja.'
                    );

                    this.isLoading.set(false);
                }
            });
    }
}