import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MovimientoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/movimientos`;

  registrarIngreso(idProducto: number, cantidad: number, motivo: string, idUsuario: number): Observable<any> {
    const params = new HttpParams()
      .set('idProducto', idProducto.toString())
      .set('cantidad', cantidad.toString())
      .set('motivo', motivo)
      .set('idUsuario', idUsuario.toString());

    // backend returns a String, we need responseType: 'text' to avoid JSON parse errors
    return this.http.post(`${this.apiUrl}/ingreso`, null, { params, responseType: 'text' });
  }
}
