import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import {
    CompraRequest,
    ProveedorDTO,
    ProductoDTO
} from '../models';

import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class InventarioService {

    private http = inject(HttpClient);

    private apiUrl = `${environment.apiUrl}/inventario`;
    private proveedoresUrl = `${environment.apiUrl}/proveedores`;
    private productosUrl = `${environment.apiUrl}/productos`;

    // ==========================================
    // PROVEEDORES
    // ==========================================

    obtenerProveedores(): Observable<ProveedorDTO[]> {

        return this.http
            .get<any>(this.proveedoresUrl)
            .pipe(
                map(response => response?.content ?? response ?? [])
            );
    }

    // ==========================================
    // BUSCAR PRODUCTOS
    // ==========================================

    buscarProductos(
        termino: string
    ): Observable<ProductoDTO[]> {

        const params = new HttpParams()
            .set('termino', termino.trim())
            .set('page', '0')
            .set('size', '20');

        return this.http
            .get<any>(`${this.productosUrl}/buscar`, { params })
            .pipe(
                map(response => response?.content ?? response ?? [])
            );
    }

    // ==========================================
    // REGISTRAR COMPRA
    // ==========================================

    registrarCompra(
        compra: CompraRequest
    ): Observable<any> {

        return this.http.post(
            `${this.apiUrl}/compras`,
            compra
        );
    }
}