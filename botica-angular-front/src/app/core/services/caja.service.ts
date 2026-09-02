import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
    CajaSesionDTO,
    CajaSesionRequest,
    CajaSesionCierreRequest
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
}