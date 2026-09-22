import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { KardexDTO, LoteDTO, Page } from '../models';

@Injectable({ providedIn: 'root' })
export class MovimientoService {
  private http = inject(HttpClient);
  private inventarioUrl = `${environment.apiUrl}/inventario`;
  private lotesUrl = `${environment.apiUrl}/lotes`;

  listarKardex(productoId?: number, loteId?: number): Observable<KardexDTO[]> {
    let params = new HttpParams().set('page', '0').set('size', '100');
    if (productoId) params = params.set('productoId', productoId);
    if (loteId) params = params.set('loteId', loteId);
    return this.http.get<Page<KardexDTO>>(`${this.inventarioUrl}/kardex`, { params })
      .pipe(map(response => response.content ?? []));
  }

  listarLotes(): Observable<LoteDTO[]> {
    return this.http.get<LoteDTO[]>(this.lotesUrl);
  }

  registrarConteo(idLote: number, cantidadContada: number, motivo: string): Observable<any> {
    return this.http.post(`${this.inventarioUrl}/conteos`, {
      motivo,
      items: [{ idLote, cantidadContada }]
    });
  }
}
