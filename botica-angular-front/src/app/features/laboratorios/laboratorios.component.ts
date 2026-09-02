import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LaboratoriosService } from '../../core/services/laboratorios.service';
import { LaboratorioDTO, LaboratorioRequest } from '../../core/models';

@Component({
    selector: 'app-laboratorios',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './laboratorios.component.html',
    styleUrl: './laboratorios.component.scss'
})
export class LaboratoriosComponent implements OnInit {
    private fb = inject(FormBuilder);
    private laboratoriosService = inject(LaboratoriosService);

    laboratorios = signal<LaboratorioDTO[]>([]);
    mostrandoFormulario = signal(false);
    isSubmitting = signal(false);

    labForm = this.fb.nonNullable.group({
        nombre: ['', [Validators.required]],
        descripcion: ['']
    });

    ngOnInit() {
        this.cargarLaboratorios();
    }

    cargarLaboratorios() {
        this.laboratoriosService.listarLaboratorios().subscribe(data => this.laboratorios.set(data));
    }

    guardarLaboratorio() {
        if (this.labForm.valid) {
            this.isSubmitting.set(true);
            const req = this.labForm.getRawValue() as LaboratorioRequest;

            this.laboratoriosService.guardarLaboratorio(req).subscribe({
                next: () => {
                    alert('Laboratorio registrado exitosamente.');
                    this.cargarLaboratorios();
                    this.mostrandoFormulario.set(false);
                    this.isSubmitting.set(false);
                },
                error: () => {
                    alert('Error al registrar.');
                    this.isSubmitting.set(false);
                }
            });
        }
    }
}