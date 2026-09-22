import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InventarioService } from '../../core/services/inventario.service';
import {
  CompraDTO, CompraRequest, ProductoDTO, ProveedorDTO, RecepcionCompraDTO
} from '../../core/models';

@Component({
  selector: 'app-compras',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './compras.component.html',
  styleUrl: './compras.component.scss'
})
export class ComprasComponent implements OnInit {
  private fb = inject(FormBuilder);
  private inventario = inject(InventarioService);

  vista = signal<'ordenes' | 'nueva'>('ordenes');
  proveedores = signal<ProveedorDTO[]>([]);
  productosEncontrados = signal<ProductoDTO[]>([]);
  productoSeleccionado = signal<ProductoDTO | null>(null);
  lineas = signal<{ producto: ProductoDTO; cantidad: number; costoUnitario: number }[]>([]);
  compras = signal<CompraDTO[]>([]);
  compraSeleccionada = signal<CompraDTO | null>(null);
  recepciones = signal<RecepcionCompraDTO[]>([]);
  isSubmitting = signal(false);
  mensaje = signal('');
  searchControl = this.fb.control('');

  ordenForm = this.fb.nonNullable.group({
    idProveedor: [0, [Validators.required, Validators.min(1)]],
    documento: ['', [Validators.required, Validators.maxLength(50)]],
    fechaEsperada: [''],
    observaciones: ['']
  });

  lineaForm = this.fb.nonNullable.group({
    cantidad: [1, [Validators.required, Validators.min(1)]],
    costoUnitario: [0, [Validators.required, Validators.min(0)]]
  });

  recepcionForm = this.fb.nonNullable.group({
    idDetalleCompra: [0, [Validators.required, Validators.min(1)]],
    cantidad: [1, [Validators.required, Validators.min(1)]],
    codigoLote: ['', [Validators.required]],
    fechaVencimiento: ['', [Validators.required]],
    documentoProveedor: [''],
    observaciones: ['']
  });

  devolucionForm = this.fb.nonNullable.group({
    lineaRecibida: ['', [Validators.required]],
    cantidad: [1, [Validators.required, Validators.min(1)]],
    motivo: ['', [Validators.required]],
    documentoReferencia: ['']
  });

  ngOnInit(): void {
    this.inventario.obtenerProveedores().subscribe(data => this.proveedores.set(data));
    this.cargarCompras();
  }

  cargarCompras(): void {
    this.inventario.listarCompras().subscribe({
      next: compras => this.compras.set(compras),
      error: err => this.error(err, 'No se pudo cargar las compras.')
    });
  }

  buscarProducto(): void {
    const termino = this.searchControl.value?.trim() || '';
    if (termino.length < 2) return;
    this.inventario.buscarProductos(termino).subscribe(data => this.productosEncontrados.set(data));
  }

  seleccionarProducto(producto: ProductoDTO): void {
    this.productoSeleccionado.set(producto);
    this.productosEncontrados.set([]);
    this.searchControl.setValue('');
  }

  agregarLinea(): void {
    const producto = this.productoSeleccionado();
    if (!producto || this.lineaForm.invalid) return;
    if (this.lineas().some(linea => linea.producto.idProducto === producto.idProducto)) {
      this.mensaje.set('El producto ya fue agregado a la orden.');
      return;
    }
    const valor = this.lineaForm.getRawValue();
    this.lineas.update(items => [...items, { producto, ...valor }]);
    this.productoSeleccionado.set(null);
    this.lineaForm.reset({ cantidad: 1, costoUnitario: 0 });
  }

  quitarLinea(idProducto: number): void {
    this.lineas.update(items => items.filter(item => item.producto.idProducto !== idProducto));
  }

  crearOrden(): void {
    if (this.ordenForm.invalid || this.lineas().length === 0) {
      this.ordenForm.markAllAsTouched();
      this.mensaje.set('Completa la cabecera y agrega al menos un producto.');
      return;
    }
    const cabecera = this.ordenForm.getRawValue();
    const request: CompraRequest = {
      ...cabecera,
      fechaEsperada: cabecera.fechaEsperada || undefined,
      idempotencyKey: crypto.randomUUID(),
      detalles: this.lineas().map(linea => ({
        idProducto: linea.producto.idProducto,
        cantidad: linea.cantidad,
        costoUnitario: linea.costoUnitario
      }))
    };
    this.isSubmitting.set(true);
    this.inventario.registrarCompra(request).subscribe({
      next: () => {
        this.mensaje.set('Orden registrada. El stock cambiará cuando confirmes la recepción.');
        this.ordenForm.reset({ idProveedor: 0, documento: '', fechaEsperada: '', observaciones: '' });
        this.lineas.set([]);
        this.vista.set('ordenes');
        this.isSubmitting.set(false);
        this.cargarCompras();
      },
      error: err => { this.isSubmitting.set(false); this.error(err, 'No se pudo registrar la orden.'); }
    });
  }

  seleccionarCompra(compra: CompraDTO): void {
    this.compraSeleccionada.set(compra);
    const primerPendiente = compra.detalles.find(d => d.cantidadPendiente > 0);
    this.recepcionForm.patchValue({ idDetalleCompra: primerPendiente?.idDetalleCompra || 0 });
    this.inventario.listarRecepciones(compra.idCompra).subscribe(data => this.recepciones.set(data));
  }

  recibir(): void {
    const compra = this.compraSeleccionada();
    if (!compra || this.recepcionForm.invalid) return;
    const raw = this.recepcionForm.getRawValue();
    this.isSubmitting.set(true);
    this.inventario.recibirCompra(compra.idCompra, {
      idempotencyKey: crypto.randomUUID(),
      documentoProveedor: raw.documentoProveedor || undefined,
      observaciones: raw.observaciones || undefined,
      detalles: [{
        idDetalleCompra: raw.idDetalleCompra,
        cantidad: raw.cantidad,
        codigoLote: raw.codigoLote,
        fechaVencimiento: raw.fechaVencimiento
      }]
    }).subscribe({
      next: () => {
        this.mensaje.set('Recepción confirmada y stock actualizado.');
        this.isSubmitting.set(false);
        this.recepcionForm.reset({ idDetalleCompra: 0, cantidad: 1, codigoLote: '', fechaVencimiento: '', documentoProveedor: '', observaciones: '' });
        this.refrescarSeleccion(compra.idCompra);
      },
      error: err => { this.isSubmitting.set(false); this.error(err, 'No se pudo confirmar la recepción.'); }
    });
  }

  devolverProveedor(): void {
    const compra = this.compraSeleccionada();
    if (!compra || this.devolucionForm.invalid) return;
    const raw = this.devolucionForm.getRawValue();
    const [idDetalleCompra, idLote] = raw.lineaRecibida.split('|').map(Number);
    this.isSubmitting.set(true);
    this.inventario.devolverProveedor(compra.idCompra, {
      idempotencyKey: crypto.randomUUID(),
      motivo: raw.motivo,
      documentoReferencia: raw.documentoReferencia || undefined,
      detalles: [{ idDetalleCompra, idLote, cantidad: raw.cantidad }]
    }).subscribe({
      next: () => {
        this.mensaje.set('Devolución al proveedor confirmada y descontada del lote.');
        this.isSubmitting.set(false);
        this.devolucionForm.reset({ lineaRecibida: '', cantidad: 1, motivo: '', documentoReferencia: '' });
        this.refrescarSeleccion(compra.idCompra);
      },
      error: err => { this.isSubmitting.set(false); this.error(err, 'No se pudo registrar la devolución.'); }
    });
  }

  lineasRecibidas(): { value: string; label: string }[] {
    return this.recepciones().flatMap(r => r.detalles.map(d => ({
      value: `${d.idDetalleCompra}|${d.idLote}`,
      label: `${d.producto} · lote ${d.lote} · recibido ${d.cantidad}`
    })));
  }

  totalOrden(): number {
    return this.lineas().reduce((total, linea) => total + linea.cantidad * linea.costoUnitario, 0);
  }

  private refrescarSeleccion(idCompra: number): void {
    this.inventario.listarCompras().subscribe(compras => {
      this.compras.set(compras);
      const compra = compras.find(c => c.idCompra === idCompra) || null;
      this.compraSeleccionada.set(compra);
      if (compra) this.inventario.listarRecepciones(idCompra).subscribe(data => this.recepciones.set(data));
    });
  }

  private error(err: any, fallback: string): void {
    this.mensaje.set(err?.error?.message || fallback);
  }
}
