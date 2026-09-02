import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// Puedes agregar esta interfaz en tu models.ts luego
export interface Categoria {
  idCategoria?: number;
  nombre: string;
  descripcion: string;
  estado?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CategoriasService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/categorias`;

  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl);
  }

  guardarCategoria(categoria: Categoria): Observable<Categoria> {
    return this.http.post<Categoria>(this.apiUrl, categoria);
  }
}