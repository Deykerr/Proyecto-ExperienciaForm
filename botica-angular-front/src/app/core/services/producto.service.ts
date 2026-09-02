import {
  inject,
  Injectable
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  map
} from 'rxjs/operators';

import {
  ProductoDTO,
  ProductoRequest,
  CategoriaDTO
} from '../models';

import {
  environment
} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductosService {

  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/productos`;

  // ==========================================
  // LISTAR PRODUCTOS
  // ==========================================

  listarProductos(): Observable<ProductoDTO[]> {

    return this.http
      .get<any>(this.apiUrl)
      .pipe(
        map(response => response?.content ?? [])
      );
  }

  // ==========================================
  // CREAR PRODUCTO
  // ==========================================

  crearProducto(
    producto: ProductoRequest
  ): Observable<ProductoDTO> {

    return this.http.post<ProductoDTO>(
      this.apiUrl,
      producto
    );
  }

  // ==========================================
  // CATEGORÍAS
  // ==========================================

  obtenerCategorias(): Observable<CategoriaDTO[]> {

    return this.http.get<CategoriaDTO[]>(
      `${environment.apiUrl}/categorias`
    );
  }
}