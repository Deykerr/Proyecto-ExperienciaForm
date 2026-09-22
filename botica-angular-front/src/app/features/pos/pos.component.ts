import {
    Component,
    computed,
    inject,
    signal
} from '@angular/core';

import {
    CurrencyPipe
} from '@angular/common';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
    FormBuilder,
    ReactiveFormsModule,
    Validators
} from '@angular/forms';

import {
    VentasService
} from '../../core/services/venta.service';

import {
    TicketService
} from '../../core/services/ticket.service';

import {
    AlertService
} from '../../core/services/alert.service';

import {
    AuthService
} from '../../core/services/auth.service';

import {
    ProductoDTO,
    ClienteDTO,
    VentaRequest
    , RecetaDTO
} from '../../core/models';


interface CartItem {
    producto: ProductoDTO;
    cantidad: number;
}


@Component({
    selector: 'app-pos',
    standalone: true,

    imports: [
        ReactiveFormsModule,
        CurrencyPipe,
        CommonModule,
        FormsModule
    ],

    templateUrl: './pos.component.html',
    styleUrl: './pos.component.scss'
})
export class PosComponent {

    private fb = inject(FormBuilder);

    private ventasService =
        inject(VentasService);

    private authService =
        inject(AuthService);

    private ticketService =
        inject(TicketService);

    private alertService =
        inject(AlertService);


    // =====================================================
    // ESTADO
    // =====================================================

    carrito = signal<CartItem[]>([]);

    clienteSeleccionado =
        signal<ClienteDTO | null>(null);

    resultadosBusqueda =
        signal<ProductoDTO[]>([]);

    isProcessing =
        signal(false);

    buscandoProducto =
        signal(false);

    buscandoCliente =
        signal(false);

    mensajeError =
        signal('');

    terminoBusqueda =
        signal('');


    // =====================================================
    // TOTAL
    // =====================================================

    getItemTotal(item: CartItem): number {
        const prod = item.producto;
        if (prod.unidadesPorPresentacion && prod.unidadesPorPresentacion > 1 && prod.precioPresentacion) {
            const cajas = Math.floor(item.cantidad / prod.unidadesPorPresentacion);
            const sueltas = item.cantidad % prod.unidadesPorPresentacion;
            return (cajas * prod.precioPresentacion) + (sueltas * prod.precioVenta);
        }
        return prod.precioVenta * item.cantidad;
    }

    totalVenta = computed(() => {
        return this.carrito().reduce(
            (total, item) => total + this.getItemTotal(item),
            0
        );
    });


    subtotalVenta = computed(() => {
        return this.carrito().reduce((total, item) => {
            const importe = this.getItemTotal(item);
            return total + (item.producto.tipoAfectacionIgv === '10' ? importe / 1.18 : importe);
        }, 0);
    });


    igvVenta = computed(() => {
        return this.totalVenta() - this.subtotalVenta();
    });


    cantidadProductos = computed(() => {

        return this.carrito().reduce(
            (total, item) =>
                total + item.cantidad,
            0
        );

    });


    // =====================================================
    // FORMULARIOS
    // =====================================================

    searchForm =
        this.fb.nonNullable.group({

            termino: ['']

        });


    clienteForm =
        this.fb.nonNullable.group({

            documento: [
                '',
                Validators.required
            ]

        });


    // =====================================================
    // BUSCAR PRODUCTO
    // =====================================================

    buscarProducto(): void {

        const termino =
            this.searchForm
                .getRawValue()
                .termino
                .trim();

        this.terminoBusqueda.set(
            termino
        );

        if (termino.length < 2) {

            this.resultadosBusqueda.set([]);

            return;

        }

        this.buscandoProducto.set(true);

        this.ventasService
            .buscarProductos(termino)
            .subscribe({

                next: (productos) => {

                    this.resultadosBusqueda.set(
                        productos.filter(
                            producto =>
                                producto.activo
                        )
                    );

                    this.buscandoProducto.set(false);

                },

                error: (error) => {

                    console.error(
                        'Error buscando productos:',
                        error
                    );

                    this.resultadosBusqueda.set([]);

                    this.buscandoProducto.set(false);

                    this.alertService.error(
                        'No se pudieron buscar los productos',
                        'Verifica la conexión con el servidor.'
                    );

                }

            });

    }


    // =====================================================
    // AGREGAR PRODUCTO
    // =====================================================

    agregarAlCarrito(
        producto: ProductoDTO
    ): void {

        if (
            !producto.stockActual ||
            producto.stockActual <= 0
        ) {

            this.alertService.warning(
                'Producto sin stock',
                'Este producto no tiene unidades disponibles.'
            );

            return;

        }


        this.carrito.update(items => {

            const existe =
                items.find(
                    item =>
                        item.producto.idProducto ===
                        producto.idProducto
                );


            if (existe) {

                if (
                    existe.cantidad >=
                    producto.stockActual
                ) {

                    this.alertService.warning(
                        'Stock insuficiente',
                        `Solo hay ${producto.stockActual} unidades disponibles.`
                    );

                    return [...items];

                }

                existe.cantidad++;

                return [...items];

            }


            return [
                ...items,
                {
                    producto,
                    cantidad: 1
                }
            ];

        });


        this.limpiarBusqueda();

    }

    agregarPaqueteAlCarrito(producto: ProductoDTO): void {
        const cantidadAAgregar = producto.unidadesPorPresentacion || 1;

        if (!producto.stockActual || producto.stockActual < cantidadAAgregar) {
            this.alertService.warning('Stock insuficiente', `Solo hay ${producto.stockActual} unidades. Se necesitan ${cantidadAAgregar} para un paquete.`);
            return;
        }

        this.carrito.update(items => {
            const existe = items.find(item => item.producto.idProducto === producto.idProducto);
            if (existe) {
                if ((existe.cantidad + cantidadAAgregar) > producto.stockActual) {
                    this.alertService.warning('Stock insuficiente', `Solo hay ${producto.stockActual} unidades disponibles en total.`);
                    return [...items];
                }
                existe.cantidad += cantidadAAgregar;
                return [...items];
            }
            return [...items, { producto, cantidad: cantidadAAgregar }];
        });

        this.limpiarBusqueda();
    }


    // =====================================================
    // CAMBIAR CANTIDAD
    // =====================================================

    cambiarCantidad(
        idProducto: number,
        nuevaCantidad: number
    ): void {

        if (nuevaCantidad <= 0) {

            this.removerDelCarrito(
                idProducto
            );

            return;

        }


        this.carrito.update(items => {

            const item =
                items.find(
                    carritoItem =>
                        carritoItem.producto.idProducto ===
                        idProducto
                );


            if (!item) {

                return [...items];

            }


            if (
                nuevaCantidad >
                item.producto.stockActual
            ) {

                this.alertService.warning(
                    'Stock insuficiente',
                    `Solo hay ${item.producto.stockActual} unidades disponibles.`
                );

                return [...items];

            }


            item.cantidad =
                nuevaCantidad;

            return [...items];

        });

    }


    // =====================================================
    // AUMENTAR
    // =====================================================

    aumentarCantidad(
        item: CartItem
    ): void {

        this.cambiarCantidad(
            item.producto.idProducto,
            item.cantidad + 1
        );

    }


    // =====================================================
    // DISMINUIR
    // =====================================================

    disminuirCantidad(
        item: CartItem
    ): void {

        this.cambiarCantidad(
            item.producto.idProducto,
            item.cantidad - 1
        );

    }


    // =====================================================
    // ELIMINAR
    // =====================================================

    removerDelCarrito(
        idProducto: number
    ): void {

        this.carrito.update(
            items =>
                items.filter(
                    item =>
                        item.producto.idProducto !==
                        idProducto
                )
        );

    }


    // =====================================================
    // LIMPIAR BÚSQUEDA
    // =====================================================

    limpiarBusqueda(): void {

        this.resultadosBusqueda.set([]);

        this.searchForm.reset();

        this.terminoBusqueda.set('');

    }


    // =====================================================
    // BUSCAR CLIENTE
    // =====================================================

    buscarCliente(): void {

        const documento =
            this.clienteForm
                .getRawValue()
                .documento
                .trim();


        if (!documento) {

            this.clienteForm.markAllAsTouched();

            return;

        }


        this.buscandoCliente.set(true);

        this.clienteSeleccionado.set(null);


        this.ventasService
            .buscarCliente(documento)
            .subscribe({

                next: cliente => {

                    this.clienteSeleccionado.set(
                        cliente
                    );

                    this.buscandoCliente.set(false);

                },

                error: error => {

                    console.error(
                        'Error buscando cliente:',
                        error
                    );

                    this.clienteSeleccionado.set(null);

                    this.buscandoCliente.set(false);

                    this.alertService.warning(
                        'Cliente no encontrado',
                        'La venta continuará como Cliente Varios.'
                    );

                }

            });

    }


    // =====================================================
    // QUITAR CLIENTE
    // =====================================================

    quitarCliente(): void {

        this.clienteSeleccionado.set(null);

        this.clienteForm.reset();

    }


    // =====================================================
    // PROCESAR VENTA
    // =====================================================

    // =====================================================
    // PAGOS DIVIDIDOS
    // =====================================================
    mostrarModalPago = signal(false);
    pagosAgregados = signal<import('../../core/models').PagoRequest[]>([]);
    metodoPagoSeleccionado = signal('EFECTIVO');
    montoPagoInput = signal<number>(0);
    referenciaPagoInput = signal('');
    referenciaReceta = signal('');
    recetasDisponibles = signal<RecetaDTO[]>([]);
    idRecetaSeleccionada = signal<number | null>(null);
    private idempotencyVenta = '';

    requiereReceta = computed(() =>
        this.carrito().some(item => item.producto.requiereReceta)
    );

    recetasCompatibles = computed(() => {
        const requeridos = this.carrito().filter(item => item.producto.requiereReceta);
        return this.recetasDisponibles().filter(receta => receta.estado === 'DISPONIBLE'
            && requeridos.every(item => receta.detalles.some(detalle =>
                detalle.idProducto === item.producto.idProducto && detalle.cantidadDisponible >= item.cantidad)));
    });

    saldoRestante = computed(() => {
        let saldo = this.totalVenta();
        for (const pago of this.pagosAgregados()) {
            saldo -= Math.min(pago.montoRecibido, saldo);
        }
        return Number(Math.max(0, saldo).toFixed(2));
    });

    vueltoTotal = computed(() => {
        let saldo = this.totalVenta();
        let vuelto = 0;
        for (const pago of this.pagosAgregados()) {
            const aplicado = Math.min(pago.montoRecibido, saldo);
            if (pago.metodoPago === 'EFECTIVO') {
                vuelto += pago.montoRecibido - aplicado;
            }
            saldo -= aplicado;
        }
        return Number(vuelto.toFixed(2));
    });

    abrirModalPago() {
        if (this.carrito().length === 0) {
            this.alertService.warning('Carrito vac�o', 'Agrega al menos un producto.');
            return;
        }
        this.pagosAgregados.set([]);
        this.metodoPagoSeleccionado.set('EFECTIVO');
        this.montoPagoInput.set(this.totalVenta());
        this.referenciaPagoInput.set('');
        this.idempotencyVenta = crypto.randomUUID();
        this.idRecetaSeleccionada.set(null);
        if (this.requiereReceta()) {
            this.ventasService.listarRecetas().subscribe({
                next: recetas => this.recetasDisponibles.set(recetas),
                error: () => this.recetasDisponibles.set([])
            });
        }
        this.mostrarModalPago.set(true);
    }

    cerrarModalPago() {
        this.mostrarModalPago.set(false);
    }

    agregarPago() {
        const monto = Number(this.montoPagoInput());
        if (monto <= 0) {
            this.alertService.warning('Monto invlido', 'Debe ser mayor a 0');
            return;
        }
        const metodo = this.metodoPagoSeleccionado();
        const referencia = this.referenciaPagoInput().trim();
        if (metodo !== 'EFECTIVO' && !referencia) {
            this.alertService.warning('Referencia requerida', 'Ingresa el número de operación o autorización.');
            return;
        }
        if (metodo !== 'EFECTIVO' && monto > this.saldoRestante()) {
            this.alertService.warning('Monto excedido', 'Un pago no efectivo no puede superar el saldo restante.');
            return;
        }
        this.pagosAgregados.update(pagos => [...pagos, {
            metodoPago: metodo,
            montoRecibido: monto,
            referencia: referencia || undefined
        }]);
        this.montoPagoInput.set(this.saldoRestante());
        this.referenciaPagoInput.set('');
    }

    removerPago(index: number) {
        this.pagosAgregados.update(pagos => pagos.filter((_, i) => i !== index));
        this.montoPagoInput.set(this.saldoRestante());
    }

    procesarVenta(): void {
        if (this.pagosAgregados().length === 0 && this.montoPagoInput() > 0) {
            this.agregarPago();
        }
        if (this.saldoRestante() > 0) {
            this.alertService.warning('Pago incompleto', 'Debe agregar los pagos hasta cubrir el total.');
            return;
        }

        if (this.requiereReceta() && !this.idRecetaSeleccionada()) {
            this.alertService.warning('Receta requerida', 'Selecciona una receta vigente con saldo suficiente.');
            return;
        }

        if (
            this.carrito().length === 0
        ) {

            this.alertService.warning(
                'Carrito vacío',
                'Agrega al menos un producto antes de realizar la venta.'
            );

            return;

        }


        const usuario =
            this.authService.currentUser();


        if (!usuario) {

            this.alertService.error(
                'Sesión no válida',
                'Debes iniciar sesión nuevamente.'
            );

            return;

        }


        this.isProcessing.set(true);


        const cliente =
            this.clienteSeleccionado();


        /*
         * Se mantiene la lógica existente:
         *
         * RUC  → FACTURA
         * DNI / Cliente Varios → BOLETA
         */

        const tipoComprobante =
            cliente?.tipoDocumento === 'RUC'
                ? 'FACTURA'
                : 'BOLETA';


        const request: VentaRequest = {
            idCliente: cliente?.idCliente,
            tipoComprobante,
            idempotencyKey: this.idempotencyVenta || crypto.randomUUID(),
            referenciaReceta: this.referenciaReceta().trim() || undefined,
            idReceta: this.idRecetaSeleccionada() || undefined,
            pagos: this.pagosAgregados(),
            items: this.carrito().map(item => ({ idProducto: item.producto.idProducto, cantidad: item.cantidad }))
        };


        this.ventasService
            .registrarVenta(request)
            .subscribe({

                next: () => {

                    /*
                     * Guardamos una referencia de los
                     * datos antes de limpiar el POS.
                     */

                    const carritoVenta =
                        this.carrito();

                    const total =
                        this.totalVenta();

                    const clienteVenta =
                        this.clienteSeleccionado();


                    this.ticketService.imprimirTicket(

                        carritoVenta,

                        total,

                        clienteVenta,

                        tipoComprobante,

                        usuario.username,

                        this.subtotalVenta(),

                        this.igvVenta()

                    );


                    this.isProcessing.set(false);


                    this.alertService.success(
                        'Venta registrada',
                        'La venta fue procesada y el ticket fue generado.'
                    );


                    this.limpiarPos();

                },


                error: error => {

                    console.error(
                        'Error procesando venta:',
                        error
                    );

                    this.isProcessing.set(false);


                    const mensaje =
                        error?.error?.message ??
                        'No se pudo procesar la venta.';


                    this.alertService.error(
                        'Error al procesar la venta',
                        mensaje
                    );

                }

            });

    }


    // =====================================================
    // CANCELAR VENTA
    // =====================================================

    async cancelarVenta(): Promise<void> {

        if (
            this.carrito().length === 0
        ) {

            return;

        }


        const resultado =
            await this.alertService.confirm(

                '¿Cancelar venta?',

                'Se eliminarán todos los productos del carrito.'

            );


        if (!resultado.isConfirmed) {

            return;

        }


        this.limpiarPos();

    }


    // =====================================================
    // LIMPIAR POS
    // =====================================================

    limpiarPos(): void {

        this.carrito.set([]);

        this.clienteSeleccionado.set(null);

        this.resultadosBusqueda.set([]);

        this.searchForm.reset();

        this.clienteForm.reset();

        this.terminoBusqueda.set('');

        this.mensajeError.set('');
        this.pagosAgregados.set([]);
        this.referenciaReceta.set('');
        this.idRecetaSeleccionada.set(null);
        this.recetasDisponibles.set([]);
        this.referenciaPagoInput.set('');
        this.idempotencyVenta = '';

    }


    // =====================================================
    // UTILIDADES
    // =====================================================

    estaEnCarrito(
        idProducto: number
    ): boolean {

        return this.carrito()
            .some(
                item =>
                    item.producto.idProducto ===
                    idProducto
            );

    }


    cantidadEnCarrito(
        idProducto: number
    ): number {

        const item =
            this.carrito()
                .find(
                    carritoItem =>
                        carritoItem.producto.idProducto ===
                        idProducto
                );

        return item?.cantidad ?? 0;

    }


    get stockBajo(): number {

        return this.resultadosBusqueda()
            .filter(
                producto =>
                    producto.stockActual <=
                    producto.stockMinimo
            )
            .length;

    }

}






