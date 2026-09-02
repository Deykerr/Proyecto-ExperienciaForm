import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClienteService } from '../../core/services/cliente.service';
import { ClienteDTO, ClienteRequest } from '../../core/models';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.scss'
})
export class ClientesComponent implements OnInit {
  private fb = inject(FormBuilder);
  private clienteService = inject(ClienteService);

  // Estados Reactivos
  clientes = signal<ClienteDTO[]>([]);
  mostrandoFormulario = signal(false);
  isSubmitting = signal(false);

  // Formulario Reactivo
  clienteForm = this.fb.nonNullable.group({
    tipoDocumento: ['DNI', [Validators.required]],
    numeroDocumento: ['', [Validators.required, Validators.minLength(8)]],
    nombreRazonSocial: ['', [Validators.required, Validators.minLength(3)]],
    direccion: ['']
  });

  ngOnInit() {
    this.cargarClientes();
  }

  cargarClientes() {
    this.clienteService.listarClientes().subscribe(data => this.clientes.set(data));
  }

  abrirFormulario() {
    this.clienteForm.reset({ tipoDocumento: 'DNI' });
    this.mostrandoFormulario.set(true);
  }

  cerrarFormulario() {
    this.mostrandoFormulario.set(false);
  }

  guardarCliente() {
    if (this.clienteForm.valid) {
      this.isSubmitting.set(true);
      const request = this.clienteForm.getRawValue() as ClienteRequest;

      this.clienteService.crearCliente(request).subscribe({
        next: () => {
          alert('Cliente registrado con éxito');
          this.cargarClientes();
          this.cerrarFormulario();
          this.isSubmitting.set(false);
        },
        error: (err) => {
          alert('Error al registrar el cliente. Verifique que el documento no esté duplicado.');
          this.isSubmitting.set(false);
          console.error(err);
        }
      });
    } else {
      this.clienteForm.markAllAsTouched();
    }
  }
}