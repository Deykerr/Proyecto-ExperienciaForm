import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { LaboratorioDTO, LaboratorioRequest } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class LaboratoriosService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/laboratorios`;

    listarLaboratorios(): Observable<LaboratorioDTO[]> {
        // Spring Boot Pageable devuelve un objeto con la propiedad "content"
        return this.http.get<any>(this.apiUrl).pipe(
            map(response => response.content)
        );
    }

    guardarLaboratorio(request: LaboratorioRequest): Observable<LaboratorioDTO> {
        return this.http.post<LaboratorioDTO>(this.apiUrl, request);
    }
}