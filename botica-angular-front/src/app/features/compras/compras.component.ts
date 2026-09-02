import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InventarioService } from '../../core/services/inventario.service';
import { AuthService } from '../../core/services/auth.service';
import { ProductoDTO, ProveedorDTO, CompraRequest } from '../../core/models';

import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-compras',
    standalone: true,
    imports: [ReactiveFormsModule, CommonModule],
    templateUrl: './compras.component.html',
    styleUrl: './compras.component.scss'
})
export class ComprasComponent implements OnInit {
    private fb = inject(FormBuilder);
    private inventarioService = inject(InventarioService);
    private authService = inject(AuthService);

    // Signals para el estado
    proveedores = signal<ProveedorDTO[]>([]);
    productosEncontrados = signal<ProductoDTO[]>([]);
    productoSeleccionado = signal<ProductoDTO | null>(null);
    isSubmitting = signal(false);

    // Buscador de producto
    searchControl = this.fb.control('');

    // Formulario principal de la compra
    compraForm = this.fb.nonNullable.group({
        proveedorId: [0, [Validators.required, Validators.min(1)]],
        codigoLote: ['', [Validators.required]],
        cajas: [0, [Validators.min(0)]],
        unidadesSueltas: [0, [Validators.min(0)]],
        precioUnitario: [0, [Validators.required, Validators.min(0.01)]],
        fechaVencimiento: ['', [Validators.required]],
        motivo: ['COMPRA DE MERCADERIA', [Validators.required]]
    });

    ngOnInit() {
        this.cargarProveedores();
    }

    cargarProveedores() {
        this.inventarioService.obtenerProveedores().subscribe(data => {
            this.proveedores.set(data);
        });
    }

    buscarProducto() {
        const termino = this.searchControl.value;
        if (termino && termino.length >= 2) {
            this.inventarioService.buscarProductos(termino).subscribe(prods => {
                this.productosEncontrados.set(prods);
            });
        } else {
            this.productosEncontrados.set([]);
        }
    }

    seleccionarProducto(producto: ProductoDTO) {
        this.productoSeleccionado.set(producto);
        this.productosEncontrados.set([]);
        this.searchControl.setValue('');
    }

    registrarIngreso() {
        if (this.compraForm.valid && this.productoSeleccionado()) {
            this.isSubmitting.set(true);

            const formValue = this.compraForm.getRawValue();
            const prod = this.productoSeleccionado()!;
            const multiplier = prod.unidadesPorPresentacion && prod.unidadesPorPresentacion > 0 ? prod.unidadesPorPresentacion : 1;
            const totalUnidades = (formValue.cajas * multiplier) + formValue.unidadesSueltas;

            if (totalUnidades <= 0) {
                alert('Debe ingresar al menos una caja o unidad.');
                this.isSubmitting.set(false);
                return;
            }

            const request = {
                productoId: prod.idProducto,
                codigoLote: formValue.codigoLote,
                cantidad: totalUnidades,
                precioUnitario: formValue.precioUnitario,
                fechaVencimiento: formValue.fechaVencimiento,
                proveedorId: formValue.proveedorId,
                motivo: formValue.motivo
            };

            this.inventarioService.registrarCompra(request).subscribe({
                next: () => {
                    alert('¡Lote ingresado exitosamente! El stock ha sido actualizado.');
                    this.limpiarFormulario();
                    this.isSubmitting.set(false);
                },
                error: (err) => {
                    alert('Error al registrar la compra.');
                    console.error(err);
                    this.isSubmitting.set(false);
                }
            });
        } else {
            this.compraForm.markAllAsTouched();
            if (!this.productoSeleccionado()) {
                alert('Debe seleccionar un producto primero.');
            }
        }
    }

    limpiarFormulario() {
        this.compraForm.reset({ motivo: 'COMPRA DE MERCADERIA' });
        this.productoSeleccionado.set(null);
    }
}