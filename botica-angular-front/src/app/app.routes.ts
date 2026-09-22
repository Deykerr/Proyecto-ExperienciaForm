import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/main-layout/main-layout.component';
import { authGuard } from './core/interceptors/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { PosComponent } from './features/pos/pos.component';
import { CajaComponent } from './features/caja/caja.component';
import { ComprasComponent } from './features/compras/compras.component';
import { ProductosComponent } from './features/productos/productos.component';
import { ClientesComponent } from './features/clientes/clientes.component';
import { ProveedoresComponent } from './features/proveedores/proveedores.component';
import { UsuariosComponent } from './features/usuarios/usuarios.component';
import { CategoriasComponent } from './features/categorias/categorias.component';
import { LaboratoriosComponent } from './features/laboratorios/laboratorios.component';
import { ReportesComponent } from './features/reportes/reportes.component';
import { MovimientosComponent } from './features/movimientos/movimientos.component';
import { OperacionesVentaComponent } from './features/operaciones-venta/operaciones-venta.component';
import { CumplimientoComponent } from './features/cumplimiento/cumplimiento.component';
import { AlertasInventarioComponent } from './features/alertas-inventario/alertas-inventario.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: 'dashboard',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      // Operaciones Diarias
      { path: 'pos', component: PosComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'CAJERO'] } },
      { path: 'caja', component: CajaComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'CAJERO'] } },
      { path: 'operaciones-venta', component: OperacionesVentaComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'CAJERO'] } },

      // Inventario y Almacén
      { path: 'compras', component: ComprasComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'ALMACENERO'] } },
      { path: 'inventario', component: MovimientosComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'ALMACENERO'] } },
      { path: 'alertas', component: AlertasInventarioComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'ALMACENERO'] } },

      // Mantenimientos (Catálogos)
      { path: 'productos', component: ProductosComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'ALMACENERO'] } },
      { path: 'clientes', component: ClientesComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'CAJERO'] } },
      { path: 'proveedores', component: ProveedoresComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'ALMACENERO'] } },
      
      // Solo ADMIN
      { path: 'usuarios', component: UsuariosComponent, canActivate: [authGuard], data: { roles: ['ADMIN'] } },
      { path: 'categorias', component: CategoriasComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'ALMACENERO'] } },
      { path: 'laboratorios', component: LaboratoriosComponent, canActivate: [authGuard], data: { roles: ['ADMIN', 'ALMACENERO'] } },
      { path: 'reportes', component: ReportesComponent, canActivate: [authGuard], data: { roles: ['ADMIN'] } },
      { path: 'cumplimiento', component: CumplimientoComponent, canActivate: [authGuard], data: { roles: ['ADMIN'] } },

      // Ruta por defecto al entrar al dashboard: el authGuard redirigirá adecuadamente según rol
      { path: '', component: PosComponent, canActivate: [authGuard] }
    ]
  },

  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
