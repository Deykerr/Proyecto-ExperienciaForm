import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './register.component.html',
  styleUrls: ['../login/login.component.scss'] // Reutilizamos estilos del login
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  registerForm: FormGroup = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(4)]],
    nombre: ['', Validators.required],
    apellidos: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });
  
  errorMsg = '';
  successMsg = '';

  onSubmit() {
    if (this.registerForm.valid) {
      // Por defecto registramos usuarios con rol básico
      const userData = { ...this.registerForm.value, rol: 'USER', activo: true };
      
      this.authService.register(userData).subscribe({
        next: () => {
          this.successMsg = 'Registro exitoso. Ahora puede iniciar sesión.';
          this.errorMsg = '';
          setTimeout(() => this.router.navigate(['/auth/login']), 2000);
        },
        error: (err) => {
          this.errorMsg = 'Error al registrar la cuenta. Verifique los datos o si el usuario ya existe.';
          this.successMsg = '';
        }
      });
    }
  }
}
