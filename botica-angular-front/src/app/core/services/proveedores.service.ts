import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators'; // <-- 1. IMPORTAR MAP
import { ProveedorDTO, ProveedorRequest } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ProveedoresService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/proveedores`;

    listarProveedores(): Observable<ProveedorDTO[]> {
        // 2. EXTRAER EL "CONTENT" SI VIENE PAGINADO
        return this.http.get<any>(this.apiUrl).pipe(
            map(response => response.content ? response.content : response)
        );
    }

    crearProveedor(proveedor: ProveedorRequest): Observable<ProveedorDTO> {
        return this.http.post<ProveedorDTO>(this.apiUrl, proveedor);
    }
}