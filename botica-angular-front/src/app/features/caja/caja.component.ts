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
import { FormsModule } from '@angular/forms';

import { DatePipe, DecimalPipe } from '@angular/common';

import { CajaService } from '../../core/services/caja.service';
import { AuthService } from '../../core/services/auth.service';

import {
    CajaDetalleDTO,
    CajaSesionDTO
} from '../../core/models';

@Component({
    selector: 'app-caja',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        FormsModule,
        DatePipe,
        DecimalPipe
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

    historial = signal<CajaSesionDTO[]>([]);

    detalleSeleccionado = signal<CajaDetalleDTO | null>(null);

    cargandoHistorial = signal(false);

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
        observaciones: ['']
    });

    formMovimiento = this.fb.nonNullable.group({
        tipoMovimiento: ['EGRESO', [Validators.required]],
        monto: [0, [Validators.required, Validators.min(0.01)]],
        motivo: ['', [Validators.required, Validators.maxLength(255)]]
    });

    formFiltros = this.fb.nonNullable.group({
        desde: [''],
        hasta: [''],
        estado: ['TODAS'],
        usuario: ['']
    });

    denominaciones = [200, 100, 50, 20, 10, 5, 2, 1, 0.5, 0.2, 0.1]
        .map(denominacion => ({ denominacion, cantidad: 0 }));

    get totalContado(): number {
        return Number(this.denominaciones.reduce((total, item) => total + item.denominacion * item.cantidad, 0).toFixed(2));
    }

    // ==========================================
    // INICIO
    // ==========================================

    ngOnInit(): void {
        this.verificarCajaActiva();
        this.cargarHistorial();
    }

    // ==========================================
    // OBTENER ID DEL USUARIO
    // ==========================================

    get esAdmin(): boolean {
        return this.authService.currentUser()?.rol === 'ADMIN';
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
                    this.cargarHistorial();
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

        this.isLoading.set(true);
        this.mensajeError.set('');

        const request = {
            montoFinal: this.totalContado,
            observaciones: this.formCierre.getRawValue().observaciones,
            denominaciones: this.denominaciones.filter(item => item.cantidad > 0)
        };

        this.cajaService
            .cerrarCaja(
                cajaActual.idCajaSesion,
                request
            )
            .subscribe({

                next: (cerrada) => {

                    const diferencia = cerrada.diferencia || 0;
                    alert(diferencia === 0 ? 'Caja cerrada y cuadrada.'
                        : `Caja cerrada con ${diferencia > 0 ? 'sobrante' : 'faltante'} de S/ ${Math.abs(diferencia).toFixed(2)}. Requiere revisión.`);

                    this.cajaActiva.set(null);

                    this.formCierre.reset({ observaciones: '' });
                    this.denominaciones.forEach(item => item.cantidad = 0);

                    this.isLoading.set(false);
                    this.cargarHistorial();
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

    registrarMovimiento(): void {
        const caja = this.cajaActiva();
        if (!caja || this.formMovimiento.invalid) {
            this.formMovimiento.markAllAsTouched();
            return;
        }
        const raw = this.formMovimiento.getRawValue();
        this.isLoading.set(true);
        this.cajaService.registrarMovimiento(caja.idCajaSesion, {
            ...raw,
            tipoMovimiento: raw.tipoMovimiento as 'INGRESO' | 'EGRESO',
            idempotencyKey: crypto.randomUUID()
        }).subscribe({
            next: actualizada => {
                this.cajaActiva.set(actualizada);
                this.formMovimiento.reset({ tipoMovimiento: 'EGRESO', monto: 0, motivo: '' });
                this.isLoading.set(false);
                this.cargarHistorial();
            },
            error: err => {
                this.mensajeError.set(err?.error?.message || 'No se pudo registrar el movimiento.');
                this.isLoading.set(false);
            }
        });
    }

    cargarHistorial(): void {
        const filtros = this.formFiltros.getRawValue();
        this.cargandoHistorial.set(true);
        this.cajaService.listarHistorial({
            desde: filtros.desde || undefined,
            hasta: filtros.hasta || undefined,
            estado: filtros.estado === 'TODAS' ? undefined : filtros.estado,
            usuario: this.esAdmin ? filtros.usuario.trim() || undefined : undefined
        }).subscribe({
            next: cajas => {
                this.historial.set(cajas);
                this.cargandoHistorial.set(false);
            },
            error: err => {
                this.mensajeError.set(err?.error?.message || 'No se pudo cargar el historial de cajas.');
                this.cargandoHistorial.set(false);
            }
        });
    }

    limpiarFiltros(): void {
        this.formFiltros.reset({ desde: '', hasta: '', estado: 'TODAS', usuario: '' });
        this.cargarHistorial();
    }

    verDetalle(idCaja: number): void {
        this.cargandoHistorial.set(true);
        this.cajaService.obtenerDetalleHistorial(idCaja).subscribe({
            next: detalle => {
                this.detalleSeleccionado.set(detalle);
                this.cargandoHistorial.set(false);
            },
            error: err => {
                this.mensajeError.set(err?.error?.message || 'No se pudo cargar el detalle de la caja.');
                this.cargandoHistorial.set(false);
            }
        });
    }

    cerrarDetalle(): void {
        this.detalleSeleccionado.set(null);
    }
}
