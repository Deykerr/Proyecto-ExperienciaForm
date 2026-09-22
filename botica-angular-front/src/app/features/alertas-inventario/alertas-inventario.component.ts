import { CommonModule, DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlertaInventarioDTO, ResumenAlertasInventarioDTO, SeveridadAlertaInventario, TipoAlertaInventario } from '../../core/models';
import { AlertasInventarioService } from '../../core/services/alertas-inventario.service';

type FiltroTipo = 'TODAS' | TipoAlertaInventario;
type FiltroSeveridad = 'TODAS' | SeveridadAlertaInventario;

@Component({
  selector: 'app-alertas-inventario',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, RouterLink],
  templateUrl: './alertas-inventario.component.html',
  styleUrl: './alertas-inventario.component.scss'
})
export class AlertasInventarioComponent implements OnInit {
  private readonly alertasService = inject(AlertasInventarioService);

  readonly resumen = signal<ResumenAlertasInventarioDTO | null>(null);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly busqueda = signal('');
  readonly filtroTipo = signal<FiltroTipo>('TODAS');
  readonly filtroSeveridad = signal<FiltroSeveridad>('TODAS');
  readonly diasVencimiento = signal(30);
  readonly opcionesDias = [7, 30, 60, 90];

  readonly alertasFiltradas = computed(() => {
    const termino = this.busqueda().trim().toLocaleLowerCase('es-PE');
    const tipo = this.filtroTipo();
    const severidad = this.filtroSeveridad();

    return (this.resumen()?.alertas ?? []).filter(alerta => {
      const coincideTipo = tipo === 'TODAS' || alerta.tipo === tipo;
      const coincideSeveridad = severidad === 'TODAS' || alerta.severidad === severidad;
      const texto = `${alerta.producto} ${alerta.codigoBarras ?? ''} ${alerta.codigoLote ?? ''}`.toLocaleLowerCase('es-PE');
      return coincideTipo && coincideSeveridad && (!termino || texto.includes(termino));
    });
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.alertasService.obtenerAlertas(this.diasVencimiento()).subscribe({
      next: resumen => {
        this.resumen.set(resumen);
        this.cargando.set(false);
      },
      error: err => {
        this.error.set(err?.error?.message || 'No se pudieron consultar las alertas de inventario.');
        this.cargando.set(false);
      }
    });
  }

  cambiarDias(valor: string | number): void {
    this.diasVencimiento.set(Number(valor));
    this.cargar();
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.filtroTipo.set('TODAS');
    this.filtroSeveridad.set('TODAS');
  }

  etiquetaTipo(tipo: TipoAlertaInventario): string {
    const etiquetas: Record<TipoAlertaInventario, string> = {
      AGOTADO: 'Producto agotado',
      STOCK_BAJO: 'Stock bajo',
      LOTE_VENCIDO: 'Lote vencido',
      LOTE_POR_VENCER: 'Lote por vencer'
    };
    return etiquetas[tipo];
  }

  iconoTipo(tipo: TipoAlertaInventario): string {
    return tipo === 'AGOTADO' ? '⛔' : tipo === 'STOCK_BAJO' ? '📉' : tipo === 'LOTE_VENCIDO' ? '☣️' : '⏳';
  }

  esAlertaStock(alerta: AlertaInventarioDTO): boolean {
    return alerta.tipo === 'AGOTADO' || alerta.tipo === 'STOCK_BAJO';
  }
}
