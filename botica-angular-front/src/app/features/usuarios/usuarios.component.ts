import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsuariosService } from '../../core/services/usuarios.service';
import { UsuarioDTO } from '../../core/models';

@Component({
    selector: 'app-usuarios',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './usuarios.component.html',
    styleUrl: './usuarios.component.scss'
})
export class UsuariosComponent implements OnInit {
    private fb = inject(FormBuilder);
    private usuariosService = inject(UsuariosService);

    // Estados Reactivos
    usuarios = signal<UsuarioDTO[]>([]);
    mostrandoFormulario = signal(false);
    isSubmitting = signal(false);

    // Formulario Reactivo
    usuarioForm = this.fb.nonNullable.group({
        nombreCompleto: ['', [Validators.required, Validators.minLength(3)]],
        username: ['', [Validators.required, Validators.minLength(4)]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        rol: ['CAJERO', [Validators.required]]
    });

    ngOnInit() {
        this.cargarUsuarios();
    }

    cargarUsuarios() {
        this.usuariosService.listarUsuarios().subscribe({
            next: (data) => this.usuarios.set(data),
            error: (err) => console.error('Error cargando usuarios', err)
        });
    }

    abrirFormulario() {
        this.usuarioForm.reset({ rol: 'CAJERO' });
        this.mostrandoFormulario.set(true);
    }

    cerrarFormulario() {
        this.mostrandoFormulario.set(false);
    }

    guardarUsuario() {
        if (this.usuarioForm.valid) {
            this.isSubmitting.set(true);
            const request = this.usuarioForm.getRawValue();

            this.usuariosService.crearUsuario(request).subscribe({
                next: () => {
                    alert('Usuario creado con éxito en el sistema.');
                    this.cargarUsuarios();
                    this.cerrarFormulario();
                    this.isSubmitting.set(false);
                },
                error: (err) => {
                    alert('Error al crear el usuario. Verifica que el username no esté en uso.');
                    this.isSubmitting.set(false);
                    console.error(err);
                }
            });
        } else {
            this.usuarioForm.markAllAsTouched();
        }
    }
}