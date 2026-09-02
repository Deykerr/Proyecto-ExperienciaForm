import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthRequest, AuthResponse } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}/auth`; // Ajusta según tu endpoint real

  // Usamos un Signal para mantener el estado del usuario globalmente
  currentUser = signal<AuthResponse | null>(this.getUserFromStorage());

  login(credentials: AuthRequest) {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        // Guardamos en local storage
        localStorage.setItem('auth_token', response.token);
        localStorage.setItem('auth_user', JSON.stringify(response));
        // Actualizamos el Signal
        this.currentUser.set(response);
      })
    );
  }

  logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  private getUserFromStorage(): AuthResponse | null {
    const userStr = localStorage.getItem('auth_user');
    return userStr ? JSON.parse(userStr) : null;
  }
}