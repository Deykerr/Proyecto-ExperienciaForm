import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { OperacionesFarmaciaService } from '../../core/services/operaciones-farmacia.service';
import { ClienteDTO, ProductoDTO, RecetaDTO, VentaResumenDTO } from '../../core/models';

@Component({
  selector: 'app-operaciones-venta',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, FormsModule, ReactiveFormsModule],
  templateUrl: './operaciones-venta.component.html',
  styleUrl: './operaciones-venta.component.scss'
})
export class OperacionesVentaComponent implements OnInit {
  private fb = inject(FormBuilder);
  private service = inject(OperacionesFarmaciaService);
  private auth = inject(AuthService);

  tab = signal<'ventas' | 'recetas'>('ventas');
  ventas = signal<VentaResumenDTO[]>([]);
  ventaSeleccionada = signal<VentaResumenDTO | null>(null);
  recetas = signal<RecetaDTO[]>([]);
  clientes = signal<ClienteDTO[]>([]);
  productos = signal<ProductoDTO[]>([]);
  productoReceta = signal<ProductoDTO | null>(null);
  detallesReceta = signal<{ producto: ProductoDTO; cantidadAutorizada: number; indicaciones: string }[]>([]);
  mensaje = signal('');
  procesando = signal(false);
  cantidadesDevolucion: Record<number, number> = {};
  busquedaProducto = this.fb.control('');

  devolucionForm = this.fb.nonNullable.group({
    motivo: ['', [Validators.required, Validators.maxLength(300)]],
    metodoReembolso: ['EFECTIVO', [Validators.required]],
    referenciaReembolso: ['']
  });

  recetaForm = this.fb.nonNullable.group({
    numero: ['', [Validators.required]], idCliente: [0], pacienteNombre: ['', [Validators.required]],
    pacienteDocumento: [''], medicoNombre: ['', [Validators.required]],
    medicoColegiatura: ['', [Validators.required]], fechaEmision: [new Date().toISOString().slice(0, 10), [Validators.required]],
    fechaVencimiento: [''], condicionVenta: ['CON_RECETA_MEDICA', [Validators.required]],
    observaciones: [''], cantidadAutorizada: [1, [Validators.min(1)]], indicaciones: ['']
  });

  ngOnInit(): void { this.cargarTodo(); }
  get esAdmin(): boolean { return this.auth.currentUser()?.rol === 'ADMIN'; }

  cargarTodo(): void {
    this.service.listarVentas().subscribe({ next: data => this.ventas.set(data), error: err => this.error(err) });
    this.service.listarRecetas().subscribe({ next: data => this.recetas.set(data), error: err => this.error(err) });
    this.service.listarClientes().subscribe({ next: data => this.clientes.set(data), error: () => this.clientes.set([]) });
  }

  seleccionarVenta(venta: VentaResumenDTO): void {
    this.ventaSeleccionada.set(venta);
    this.cantidadesDevolucion = {};
    venta.detalles.forEach(d => this.cantidadesDevolucion[d.idDetalleVenta] = 0);
    this.devolucionForm.reset({ motivo: '', metodoReembolso: 'EFECTIVO', referenciaReembolso: '' });
  }

  devolver(anular = false): void {
    const venta = this.ventaSeleccionada();
    if (!venta || this.devolucionForm.invalid) return;
    const pago = this.devolucionForm.getRawValue();
    if (pago.metodoReembolso !== 'EFECTIVO' && !pago.referenciaReembolso.trim()) {
      this.mensaje.set('La referencia es obligatoria para reembolsos no efectivos.'); return;
    }
    const base = { idempotencyKey: crypto.randomUUID(), motivo: pago.motivo,
      metodoReembolso: pago.metodoReembolso, referenciaReembolso: pago.referenciaReembolso || undefined };
    const items = venta.detalles.filter(d => (this.cantidadesDevolucion[d.idDetalleVenta] || 0) > 0)
      .map(d => ({ idDetalleVenta: d.idDetalleVenta, cantidad: this.cantidadesDevolucion[d.idDetalleVenta] }));
    if (!anular && items.length === 0) { this.mensaje.set('Selecciona al menos una cantidad para devolver.'); return; }
    const operacion = anular ? this.service.anularVenta(venta.idVenta, base)
      : this.service.devolverVenta(venta.idVenta, { ...base, items });
    this.procesando.set(true);
    operacion.subscribe({
      next: () => {
        this.mensaje.set(anular ? 'Venta anulada y compensada integralmente.' : 'Devolución y reembolso registrados.');
        this.procesando.set(false); this.ventaSeleccionada.set(null); this.cargarTodo();
      },
      error: err => { this.procesando.set(false); this.error(err); }
    });
  }

  buscarProducto(): void {
    const termino = this.busquedaProducto.value?.trim() || '';
    if (termino.length >= 2) this.service.buscarProductos(termino).subscribe(data => this.productos.set(data));
  }

  seleccionarProducto(producto: ProductoDTO): void {
    if (!producto.requiereReceta) { this.mensaje.set('Este producto está registrado como venta sin receta.'); return; }
    this.productoReceta.set(producto); this.productos.set([]);
  }

  agregarDetalleReceta(): void {
    const producto = this.productoReceta();
    if (!producto || this.detallesReceta().some(d => d.producto.idProducto === producto.idProducto)) return;
    const raw = this.recetaForm.getRawValue();
    this.detallesReceta.update(items => [...items, { producto, cantidadAutorizada: raw.cantidadAutorizada, indicaciones: raw.indicaciones }]);
    this.productoReceta.set(null); this.recetaForm.patchValue({ cantidadAutorizada: 1, indicaciones: '' });
  }

  quitarDetalleReceta(idProducto: number): void {
    this.detallesReceta.update(items => items.filter(d => d.producto.idProducto !== idProducto));
  }

  crearReceta(): void {
    if (this.recetaForm.invalid || this.detallesReceta().length === 0) {
      this.recetaForm.markAllAsTouched(); this.mensaje.set('Completa la receta y agrega al menos un medicamento.'); return;
    }
    const raw = this.recetaForm.getRawValue();
    this.procesando.set(true);
    this.service.crearReceta({
      numero: raw.numero, idCliente: raw.idCliente || undefined, pacienteNombre: raw.pacienteNombre,
      pacienteDocumento: raw.pacienteDocumento || undefined, medicoNombre: raw.medicoNombre,
      medicoColegiatura: raw.medicoColegiatura, fechaEmision: raw.fechaEmision,
      fechaVencimiento: raw.fechaVencimiento || undefined, condicionVenta: raw.condicionVenta,
      observaciones: raw.observaciones || undefined,
      detalles: this.detallesReceta().map(d => ({ idProducto: d.producto.idProducto,
        cantidadAutorizada: d.cantidadAutorizada, indicaciones: d.indicaciones || undefined }))
    }).subscribe({
      next: () => {
        this.mensaje.set('Receta registrada y disponible en el POS.'); this.procesando.set(false); this.detallesReceta.set([]);
        this.recetaForm.reset({ numero: '', idCliente: 0, pacienteNombre: '', pacienteDocumento: '', medicoNombre: '',
          medicoColegiatura: '', fechaEmision: new Date().toISOString().slice(0, 10), fechaVencimiento: '',
          condicionVenta: 'CON_RECETA_MEDICA', observaciones: '', cantidadAutorizada: 1, indicaciones: '' });
        this.cargarTodo();
      },
      error: err => { this.procesando.set(false); this.error(err); }
    });
  }

  private error(err: any): void { this.mensaje.set(err?.error?.message || 'No se pudo completar la operación.'); }
}
