import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { switchMap, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthRequest, AuthResponse } from '../models';
import { environment } from '../../../environments/environment';

interface CsrfResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}/auth`;

  currentUser = signal<AuthResponse | null>(this.getUserFromStorage());

  login(credentials: AuthRequest) {
    return this.http.get<CsrfResponse>(`${this.apiUrl}/csrf`).pipe(
      tap(response => sessionStorage.setItem('csrf_token', response.token)),
      switchMap(response => this.http.post<AuthResponse>(
        `${this.apiUrl}/login`,
        credentials,
        { headers: { 'X-XSRF-TOKEN': response.token } }
      )),
      tap(response => {
        // Solo se conserva información no sensible. El JWT vive en cookie HttpOnly.
        localStorage.setItem('auth_user', JSON.stringify(response));
        this.currentUser.set(response);
      })
    );
  }

  logout(notifyServer = true) {
    if (notifyServer) {
      this.http.post<void>(`${this.apiUrl}/logout`, {}).subscribe({
        next: () => this.clearSession(),
        error: () => this.clearSession()
      });
      return;
    }
    this.clearSession();
  }

  isLoggedIn(): boolean {
    return this.currentUser() !== null;
  }

  getUsuarioId(): number | null {
    return this.currentUser()?.idUsuario ?? null;
  }

  private clearSession() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    sessionStorage.removeItem('csrf_token');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  private getUserFromStorage(): AuthResponse | null {
    const userStr = localStorage.getItem('auth_user');
    return userStr ? JSON.parse(userStr) : null;
  }
}
