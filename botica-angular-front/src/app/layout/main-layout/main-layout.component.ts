import { Component, DestroyRef, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AlertasInventarioService } from '../../core/services/alertas-inventario.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, switchMap, timer } from 'rxjs';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss']
})
export class LayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private alertasService = inject(AlertasInventarioService);
  private destroyRef = inject(DestroyRef);

  readonly alertasPendientes = this.alertasService.contador;

  sidebarColapsado = signal(false);
  sidebarMovilAbierto = signal(false);
  
  usuario = signal<{
    idUsuario: number;
    username: string;
    nombreCompleto: string;
    rol: string;
  } | null>(null);

  ngOnInit() {
    this.usuario.set(this.authService.currentUser());
    if (this.hasRole(['ADMIN', 'ALMACENERO'])) {
      timer(0, 60_000).pipe(
        switchMap(() => this.alertasService.obtenerAlertas().pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(this.destroyRef)
      ).subscribe();
    }
  }

  get inicialesUsuario(): string {
    const usr = this.usuario();
    if (!usr || !usr.nombreCompleto) return 'U';
    
    return usr.nombreCompleto
      .split(' ')
      .map(n => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  // Verificar roles
  hasRole(roles: string[]): boolean {
    const rol = this.usuario()?.rol;
    return roles.includes(rol || '');
  }

  toggleSidebar() {
    this.sidebarColapsado.update(v => !v);
  }

  abrirSidebarMovil() {
    this.sidebarMovilAbierto.set(true);
  }

  cerrarSidebarMovil() {
    this.sidebarMovilAbierto.set(false);
  }

  navegarYcerrar() {
    if (window.innerWidth <= 768) {
      this.cerrarSidebarMovil();
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
