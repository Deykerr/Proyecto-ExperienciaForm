import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductosService } from '../../core/services/producto.service';
import { ProductoDTO, ProductoRequest } from '../../core/models';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './productos.component.html',
  styleUrl: './productos.component.scss'
})
export class ProductosComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productosService = inject(ProductosService);

  // Estados
  productos = signal<ProductoDTO[]>([]);
  categorias = signal<any[]>([]);
  mostrandoFormulario = signal(false);
  isSubmitting = signal(false);

  // Formulario de Creación
  productoForm = this.fb.nonNullable.group({
    nombre: ['', [Validators.required]],
    codigoBarras: ['', [Validators.required]],
    codigoSunat: [''],
    tipoAfectacionIgv: ['10', [Validators.required]], // 10 = Gravado por defecto
    precioVenta: [0, [Validators.required, Validators.min(0)]],
    stockMinimo: [5, [Validators.required, Validators.min(1)]],
    idCategoria: [0, [Validators.required, Validators.min(1)]],
    unidadesPorPresentacion: [1, [Validators.required, Validators.min(1)]],
    precioPresentacion: [0, [Validators.min(0)]],
    requiereReceta: [false],
    condicionVenta: ['SIN_RECETA_MEDICA', [Validators.required]],
    registroSanitario: ['']
  });

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    this.productosService.listarProductos().subscribe(data => this.productos.set(data));
    this.productosService.obtenerCategorias().subscribe(data => this.categorias.set(data));
  }

  getDisplayStock(prod: ProductoDTO): string {
    if (prod.unidadesPorPresentacion && prod.unidadesPorPresentacion > 1) {
      const paquetes = Math.floor(prod.stockActual / prod.unidadesPorPresentacion);
      const sueltas = prod.stockActual % prod.unidadesPorPresentacion;
      return `${paquetes} Pqte${paquetes !== 1 ? 's' : ''} + ${sueltas} Unid`;
    }
    return `${prod.stockActual} Unid`;
  }

  abrirFormulario() {
    this.productoForm.reset({ tipoAfectacionIgv: '10', stockMinimo: 5, unidadesPorPresentacion: 1,
      precioPresentacion: 0, requiereReceta: false, condicionVenta: 'SIN_RECETA_MEDICA', registroSanitario: '' });
    this.mostrandoFormulario.set(true);
  }

  cerrarFormulario() {
    this.mostrandoFormulario.set(false);
  }

  guardarProducto() {
    if (this.productoForm.valid) {
      this.isSubmitting.set(true);
      const raw = this.productoForm.getRawValue();
      const request = {
        ...raw,
        requiereReceta: raw.condicionVenta !== 'SIN_RECETA_MEDICA'
      } as ProductoRequest;

      this.productosService.crearProducto(request).subscribe({
        next: () => {
          alert('Producto registrado con éxito');
          this.cargarDatos(); // Recargar la tabla
          this.cerrarFormulario();
          this.isSubmitting.set(false);
        },
        error: (err) => {
          alert('Error al registrar el producto');
          this.isSubmitting.set(false);
        }
      });
    } else {
      this.productoForm.markAllAsTouched();
    }
  }
}
