import { inject, Injectable } from '@angular/core';

import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import {
  Observable,
  map
} from 'rxjs';

import {
  ProductoDTO,
  ClienteDTO,
  VentaRequest
} from '../models';

import {
  environment
} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VentasService {

  private http = inject(HttpClient);

  private apiUrl =
    `${environment.apiUrl}/ventas`;

  private productosUrl =
    `${environment.apiUrl}/productos`;

  private clientesUrl =
    `${environment.apiUrl}/clientes`;


  // ==========================================
  // BUSCAR PRODUCTOS
  // ==========================================

  buscarProductos(
    termino: string
  ): Observable<ProductoDTO[]> {

    // Búsqueda en el servidor con paginación
    const params = new HttpParams()
      .set('termino', termino)
      .set('page', '0')
      .set('size', '20');

    return this.http
      .get<any>(`${this.productosUrl}/buscar`, { params })
      .pipe(
        map(response => response?.content ?? [])
      );
  }


  // ==========================================
  // BUSCAR CLIENTE
  // ==========================================

  buscarCliente(
    numeroDocumento: string
  ): Observable<ClienteDTO> {

    return this.http.get<ClienteDTO>(
      `${this.clientesUrl}/${encodeURIComponent(numeroDocumento)}`
    );
  }


  // ==========================================
  // REGISTRAR VENTA
  // ==========================================

  registrarVenta(
    venta: VentaRequest
  ): Observable<any> {

    return this.http.post(
      this.apiUrl,
      venta
    );
  }
}