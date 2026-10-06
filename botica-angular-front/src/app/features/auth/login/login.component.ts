import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  AuthService
} from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  // ==========================================
  // ESTADO DE LA VISTA
  // ==========================================

  isLoading = signal(false);

  errorMessage = signal('');

  mostrarPassword = signal(false);

  // ==========================================
  // FORMULARIO
  // ==========================================

  loginForm = this.fb.nonNullable.group({

    username: [
      '',
      [
        Validators.required,
        Validators.minLength(3)
      ]
    ],

    password: [
      '',
      [
        Validators.required,
        Validators.minLength(4)
      ]
    ]

  });

  // ==========================================
  // GETTERS
  // ==========================================

  get usernameControl() {
    return this.loginForm.controls.username;
  }

  get passwordControl() {
    return this.loginForm.controls.password;
  }

  // ==========================================
  // MOSTRAR / OCULTAR PASSWORD
  // ==========================================

  togglePassword(): void {

    this.mostrarPassword.update(
      value => !value
    );

  }

  // ==========================================
  // SUBMIT
  // ==========================================

  onSubmit(): void {

    this.errorMessage.set('');

    if (this.loginForm.invalid) {

      this.loginForm.markAllAsTouched();

      return;
    }

    this.isLoading.set(true);

    this.authService
      .login(this.loginForm.getRawValue())
      .subscribe({

        next: () => {

          this.isLoading.set(false);

          this.router.navigate([
            '/dashboard'
          ]);

        },

        error: (error) => {

          console.error(
            'Error de autenticación:',
            error
          );

          this.isLoading.set(false);

          if (error?.status === 401) {

            this.errorMessage.set(
              'Usuario o contraseña incorrectos.'
            );

          } else if (error?.status === 403) {

            this.errorMessage.set(
              'No se pudo validar la sesión segura. Recarga la página e inténtalo nuevamente.'
            );

          } else if (error?.status === 0) {

            this.errorMessage.set(
              'No se pudo conectar con el servidor.'
            );

          } else {

            this.errorMessage.set(
              error?.error?.message ??
              'No se pudo iniciar sesión. Inténtalo nuevamente.'
            );

          }

        }

      });

  }

}
