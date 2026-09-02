import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MovimientoService } from '../../core/services/movimiento.service';
import { ProductoService } from '../../core/services/producto.service';
import { AuthService } from '../../core/services/auth.service';
import { Producto } from '../../core/models';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './movimientos.component.html',
  styleUrls: ['./movimientos.component.scss']
})
export class MovimientosComponent implements OnInit {
  private movimientoService = inject(MovimientoService);
  private productoService = inject(ProductoService);
  public authService = inject(AuthService);
  private fb = inject(FormBuilder);

  productos: Producto[] = [];
  movimientoForm: FormGroup;
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  constructor() {
    this.movimientoForm = this.fb.group({
      idProducto: ['', Validators.required],
      cantidad: [0, [Validators.required, Validators.min(1)]],
      motivo: ['Ingreso Inicial', Validators.required]
    });
  }

  ngOnInit() {
    this.cargarProductos();
  }

  cargarProductos() {
    this.productoService.listar().subscribe({
      next: (res: any) => {
        // En Movimientos cargamos todos los productos (incluso los de stock 0)
        this.productos = res.content ? res.content : res;
      },
      error: (err) => console.error('Error cargando productos', err)
    });
  }

  registrarIngreso() {
    if (this.movimientoForm.invalid) return;

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const { idProducto, cantidad, motivo } = this.movimientoForm.value;
    const idUsuario = this.authService.getUsuarioId() || 1;

    this.movimientoService.registrarIngreso(idProducto, cantidad, motivo, idUsuario).subscribe({
      next: (res) => {
        this.successMessage = res; // "Stock ingresado correctamente"
        this.isSubmitting = false;
        this.movimientoForm.reset({ motivo: 'Ingreso Extra' });
        
        // Hide message after 3 seconds
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Hubo un error al registrar el stock.';
        this.isSubmitting = false;
      }
    });
  }
}
