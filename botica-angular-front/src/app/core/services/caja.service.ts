import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
    CajaSesionDTO,
    CajaDetalleDTO,
    CajaSesionRequest,
    CajaSesionCierreRequest
    , MovimientoCajaRequest
} from '../models';

import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class CajaService {

    private http = inject(HttpClient);

    private apiUrl = `${environment.apiUrl}/caja`;

    obtenerCajaActiva(): Observable<CajaSesionDTO> {
        return this.http.get<CajaSesionDTO>(`${this.apiUrl}/actual`);
    }

    abrirCaja(request: CajaSesionRequest): Observable<CajaSesionDTO> {
        return this.http.post<CajaSesionDTO>(`${this.apiUrl}/abrir`, request);
    }

    cerrarCaja(
        idCajaSesion: number,
        request: CajaSesionCierreRequest
    ): Observable<CajaSesionDTO> {

        return this.http.post<CajaSesionDTO>(
            `${this.apiUrl}/cerrar/${idCajaSesion}`,
            request
        );
    }

    registrarMovimiento(idCajaSesion: number, request: MovimientoCajaRequest): Observable<CajaSesionDTO> {
        return this.http.post<CajaSesionDTO>(`${this.apiUrl}/${idCajaSesion}/movimientos`, request);
    }

    listarHistorial(filtros: {
        desde?: string;
        hasta?: string;
        estado?: string;
        usuario?: string;
    } = {}): Observable<CajaSesionDTO[]> {
        let params = new HttpParams();
        Object.entries(filtros).forEach(([clave, valor]) => {
            if (valor) params = params.set(clave, valor);
        });
        return this.http.get<CajaSesionDTO[]>(`${this.apiUrl}/historial`, { params });
    }

    obtenerDetalleHistorial(idCajaSesion: number): Observable<CajaDetalleDTO> {
        return this.http.get<CajaDetalleDTO>(`${this.apiUrl}/historial/${idCajaSesion}`);
    }
}
