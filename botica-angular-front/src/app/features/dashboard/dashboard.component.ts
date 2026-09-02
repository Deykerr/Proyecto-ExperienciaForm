import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportesService } from '../../core/services/reportes.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { MetricasDTO, BajoStockDTO, LoteVencerDTO } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private reportesService = inject(ReportesService);
  private http = inject(HttpClient);

  metricas = signal<MetricasDTO | null>(null);
  bajoStock = signal<BajoStockDTO[]>([]);
  lotesVencer = signal<LoteVencerDTO[]>([]);
  historialVentas = signal<any[]>([]);

  ngOnInit(): void {
    this.reportesService.getMetricasDia().subscribe(res => this.metricas.set(res));
    this.reportesService.getProductosBajoStock().subscribe(res => this.bajoStock.set(res));
    this.reportesService.getLotesPorVencer().subscribe(res => this.lotesVencer.set(res));
    this.http.get<any[]>(`${environment.apiUrl}/ventas`).subscribe(res => this.historialVentas.set(res));
  }
}
