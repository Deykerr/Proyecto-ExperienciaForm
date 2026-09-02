import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MetricasDTO, BajoStockDTO, LoteVencerDTO } from '../models';

@Injectable({
    providedIn: 'root'
})
export class ReportesService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/reportes`;

    getMetricasDia(): Observable<MetricasDTO> {
        return this.http.get<MetricasDTO>(`${this.apiUrl}/metricas-dia`);
    }

    getVentasSemana(): Observable<number[]> {
        return this.http.get<number[]>(`${this.apiUrl}/ventas-semana`);
    }

    getProductosBajoStock(): Observable<BajoStockDTO[]> {
        return this.http.get<BajoStockDTO[]>(`${this.apiUrl}/bajo-stock`);
    }

    getLotesPorVencer(): Observable<LoteVencerDTO[]> {
        return this.http.get<LoteVencerDTO[]>(`${this.apiUrl}/lotes-vencer`);
    }
}