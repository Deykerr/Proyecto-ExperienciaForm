import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResumenAlertasInventarioDTO } from '../models';

@Injectable({ providedIn: 'root' })
export class AlertasInventarioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/alertas/inventario`;
  private readonly contadorInterno = signal(0);

  readonly contador = this.contadorInterno.asReadonly();

  obtenerAlertas(diasVencimiento = 30, sincronizarContador = diasVencimiento === 30): Observable<ResumenAlertasInventarioDTO> {
    const params = new HttpParams().set('diasVencimiento', diasVencimiento);
    return this.http.get<ResumenAlertasInventarioDTO>(this.apiUrl, { params }).pipe(
      tap(resumen => {
        if (sincronizarContador) this.contadorInterno.set(resumen.total);
      })
    );
  }
}
