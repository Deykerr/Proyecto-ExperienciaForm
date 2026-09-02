import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProveedoresService } from '../../core/services/proveedores.service';
import { ProveedorDTO, ProveedorRequest } from '../../core/models';

@Component({
    selector: 'app-proveedores',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './proveedores.component.html',
    styleUrl: './proveedores.component.scss'
})
export class ProveedoresComponent implements OnInit {
    private fb = inject(FormBuilder);
    private proveedoresService = inject(ProveedoresService);

    // Signals de estado
    proveedores = signal<ProveedorDTO[]>([]);
    mostrandoFormulario = signal(false);
    isSubmitting = signal(false);

    // Formulario Reactivo
    proveedorForm = this.fb.nonNullable.group({
        ruc: ['', [Validators.required, Validators.pattern('^[0-9]{11}$')]], // RUC peruano: 11 dígitos
        razonSocial: ['', [Validators.required, Validators.minLength(3)]],
        telefono: [''],
        correo: ['', [Validators.email]],
        direccion: ['']
    });

    ngOnInit() {
        this.cargarProveedores();
    }

    cargarProveedores() {
        this.proveedoresService.listarProveedores().subscribe({
            next: (data) => this.proveedores.set(data),
            error: (err) => console.error('Error cargando proveedores', err)
        });
    }

    abrirFormulario() {
        this.proveedorForm.reset();
        this.mostrandoFormulario.set(true);
    }

    cerrarFormulario() {
        this.mostrandoFormulario.set(false);
    }

    guardarProveedor() {
        if (this.proveedorForm.valid) {
            this.isSubmitting.set(true);
            const request = this.proveedorForm.getRawValue() as ProveedorRequest;

            this.proveedoresService.crearProveedor(request).subscribe({
                next: () => {
                    alert('Proveedor registrado con éxito en la base de datos.');
                    this.cargarProveedores();
                    this.cerrarFormulario();
                    this.isSubmitting.set(false);
                },
                error: (err) => {
                    alert('Error al registrar. Verifica que el RUC no esté duplicado.');
                    this.isSubmitting.set(false);
                    console.error(err);
                }
            });
        } else {
            this.proveedorForm.markAllAsTouched();
        }
    }
}