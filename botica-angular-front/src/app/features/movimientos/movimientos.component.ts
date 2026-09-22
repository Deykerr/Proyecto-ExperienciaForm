import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MovimientoService } from '../../core/services/movimiento.service';
import { KardexDTO, LoteDTO } from '../../core/models';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './movimientos.component.html',
  styleUrls: ['./movimientos.component.scss']
})
export class MovimientosComponent implements OnInit {
  private movimientoService = inject(MovimientoService);
  private fb = inject(FormBuilder);

  lotes = signal<LoteDTO[]>([]);
  kardex = signal<KardexDTO[]>([]);
  isSubmitting = signal(false);
  mensaje = signal('');

  conteoForm = this.fb.nonNullable.group({
    idLote: [0, [Validators.required, Validators.min(1)]],
    cantidadContada: [0, [Validators.required, Validators.min(0)]],
    motivo: ['CONTEO FÍSICO', [Validators.required, Validators.maxLength(300)]]
  });

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    this.movimientoService.listarLotes().subscribe(lotes => this.lotes.set(lotes));
    this.movimientoService.listarKardex().subscribe(kardex => this.kardex.set(kardex));
  }

  registrarConteo() {
    if (this.conteoForm.invalid) {
      this.conteoForm.markAllAsTouched();
      return;
    }
    const value = this.conteoForm.getRawValue();
    this.isSubmitting.set(true);
    this.movimientoService.registrarConteo(
      value.idLote,
      value.cantidadContada,
      value.motivo
    ).subscribe({
      next: () => {
        this.mensaje.set('Conteo finalizado y kardex actualizado.');
        this.isSubmitting.set(false);
        this.cargarDatos();
      },
      error: () => {
        this.mensaje.set('No se pudo registrar el conteo.');
        this.isSubmitting.set(false);
      }
    });
  }
}
