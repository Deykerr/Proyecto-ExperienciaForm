import { Component, ElementRef, ViewChild, AfterViewInit, inject, signal, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe, CommonModule } from '@angular/common';
import Chart from 'chart.js/auto';
import { ReportesService } from '../../core/services/reportes.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
    selector: 'app-reportes',
    standalone: true,
    imports: [CurrencyPipe, DatePipe, CommonModule],
    templateUrl: './reportes.component.html',
    styleUrl: './reportes.component.scss'
})
export class ReportesComponent implements OnInit, AfterViewInit {
    @ViewChild('graficoVentas') graficoVentas!: ElementRef;

    private reportesService = inject(ReportesService);
    private http = inject(HttpClient);
    private chartInstance: Chart | null = null;

    // Estados Reactivos
    metricas = signal({ totalVentas: 0, cantidadOperaciones: 0, ticketPromedio: 0 });
    bajoStock = signal<any[]>([]);
    lotesVencer = signal<any[]>([]);
    historialVentas = signal<any[]>([]);

    ngOnInit() {
        this.cargarDatos();
    }

    ngAfterViewInit() {
        // Grafico se renderiza cuando llegan los datos en cargarDatos()
    }

    cargarDatos() {
        this.reportesService.getMetricasDia().subscribe(data => {
            if(data) this.metricas.set(data);
        });

        this.reportesService.getProductosBajoStock().subscribe(data => {
            if(data) this.bajoStock.set(data);
        });

        this.reportesService.getLotesPorVencer().subscribe(data => {
            if(data) this.lotesVencer.set(data);
        });
        
        this.reportesService.getVentasSemana().subscribe(data => {
            if(data) {
                // Asumimos que data puede ser un array de valores o un objeto con estructura
                this.renderizarGrafico(data);
            }
        });
        
        this.http.get<any[]>(`${environment.apiUrl}/ventas`).subscribe(data => {
            if(data) this.historialVentas.set(data);
        });
    }

    renderizarGrafico(ventasData?: any) {
        if (!this.graficoVentas) return;
        const ctx = this.graficoVentas.nativeElement.getContext('2d');

        // Si ya existe un gráfico, lo destruimos para evitar parpadeos al recargar
        if (this.chartInstance) {
            this.chartInstance.destroy();
        }

        // Datos por defecto en caso de fallo, idealmente extraídos de ventasData
        let chartData = [0, 0, 0, 0, 0, 0, 0];
        if (Array.isArray(ventasData) && ventasData.length > 0) {
            chartData = ventasData;
        }

        this.chartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'],
                datasets: [{
                    label: 'Ventas Diarias (S/.)',
                    data: chartData,
                    backgroundColor: '#007bff',
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });
    }
}