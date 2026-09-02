import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['../login/login.component.scss']
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  forgotForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });
  
  errorMsg = '';
  successMsg = '';
  isLoading = false;

  onSubmit() {
    if (this.forgotForm.valid) {
      this.isLoading = true;
      this.authService.forgotPassword(this.forgotForm.value.email).subscribe({
        next: () => {
          this.successMsg = 'Si el correo existe en nuestro sistema, recibirá un enlace para recuperar su contraseña.';
          this.errorMsg = '';
          this.isLoading = false;
        },
        error: () => {
          // Por seguridad, a veces se muestra el mismo mensaje de éxito
          this.successMsg = 'Si el correo existe en nuestro sistema, recibirá un enlace para recuperar su contraseña.';
          this.errorMsg = '';
          this.isLoading = false;
        }
      });
    }
  }
}
