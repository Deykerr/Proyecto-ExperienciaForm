import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DocumentoElectronicoDTO, ReporteDigemidDTO, ResumenDiarioSunatDTO } from '../../core/models';
import { OperacionesFarmaciaService } from '../../core/services/operaciones-farmacia.service';

@Component({
  selector: 'app-cumplimiento', standalone: true,
  imports: [CommonModule, DatePipe, ReactiveFormsModule],
  templateUrl: './cumplimiento.component.html', styleUrl: './cumplimiento.component.scss'
})
export class CumplimientoComponent implements OnInit {
  private fb = inject(FormBuilder);
  private service = inject(OperacionesFarmaciaService);
  tab = signal<'sunat' | 'digemid'>('sunat');
  documentos = signal<DocumentoElectronicoDTO[]>([]);
  resumenes = signal<ResumenDiarioSunatDTO[]>([]);
  reportes = signal<ReporteDigemidDTO[]>([]);
  reporteSeleccionado = signal<ReporteDigemidDTO | null>(null);
  mensaje = signal('');
  procesando = signal(false);
  periodo = this.fb.nonNullable.control(new Date().toISOString().slice(0, 7), Validators.required);
  resultadoForm = this.fb.nonNullable.group({
    estado: ['ENVIADO', Validators.required], constancia: ['', Validators.required], observaciones: ['']
  });

  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.service.listarDocumentosSunat().subscribe({ next: d => this.documentos.set(d), error: e => this.error(e) });
    this.service.listarResumenesSunat().subscribe({ next: r => this.resumenes.set(r), error: e => this.error(e) });
    this.service.listarReportesDigemid().subscribe({ next: r => this.reportes.set(r), error: e => this.error(e) });
  }
  procesarSunat(id: number): void {
    this.procesando.set(true);
    this.service.procesarDocumentoSunat(id).subscribe({
      next: d => { this.mensaje.set(`Documento ${d.serie}-${d.correlativo}: ${d.estado}. ${d.descripcionRespuesta || ''}`); this.procesando.set(false); this.cargar(); },
      error: e => { this.procesando.set(false); this.error(e); }
    });
  }
  descargarXml(id: number): void { this.service.descargarXmlSunat(id).subscribe(b => this.guardar(b, `documento-${id}.xml`)); }
  descargarCdr(id: number): void { this.service.descargarCdrSunat(id).subscribe(b => this.guardar(b, `R-documento-${id}.zip`)); }
  crearResumen(): void {
    this.procesando.set(true);
    this.service.crearResumenSunat().subscribe({
      next: r => { this.mensaje.set(`Resumen ${r.identificador}: ${r.estado}. ${r.descripcionRespuesta || ''}`); this.procesando.set(false); this.cargar(); },
      error: e => { this.procesando.set(false); this.error(e); }
    });
  }
  procesarResumen(id: number): void {
    this.procesando.set(true);
    this.service.procesarResumenSunat(id).subscribe({
      next: r => { this.mensaje.set(`Resumen ${r.identificador}: ${r.estado}. ${r.descripcionRespuesta || ''}`); this.procesando.set(false); this.cargar(); },
      error: e => { this.procesando.set(false); this.error(e); }
    });
  }
  descargarXmlResumen(id: number): void { this.service.descargarXmlResumenSunat(id).subscribe(b => this.guardar(b, `resumen-diario-${id}.xml`)); }
  descargarCdrResumen(id: number): void { this.service.descargarCdrResumenSunat(id).subscribe(b => this.guardar(b, `R-resumen-diario-${id}.zip`)); }
  generarDigemid(): void {
    if (this.periodo.invalid) return;
    this.procesando.set(true);
    this.service.generarReporteDigemid(this.periodo.value).subscribe({
      next: r => { this.mensaje.set(`Reporte ${r.periodo} generado y validado con ${r.cantidadProductos} productos.`); this.procesando.set(false); this.cargar(); },
      error: e => { this.procesando.set(false); this.error(e); }
    });
  }
  seleccionarReporte(reporte: ReporteDigemidDTO): void {
    this.reporteSeleccionado.set(reporte);
    this.resultadoForm.reset({ estado: 'ENVIADO', constancia: reporte.constancia || '', observaciones: reporte.observaciones || '' });
  }
  registrarResultado(): void {
    const reporte = this.reporteSeleccionado();
    if (!reporte || this.resultadoForm.invalid) return;
    this.service.registrarResultadoDigemid(reporte.idReporte, this.resultadoForm.getRawValue()).subscribe({
      next: r => { this.mensaje.set(`Resultado ${r.estado} registrado con constancia.`); this.reporteSeleccionado.set(null); this.cargar(); },
      error: e => this.error(e)
    });
  }
  descargarDigemid(id: number, periodo: string): void {
    this.service.descargarReporteDigemid(id).subscribe(b => this.guardar(b, `reporte-precios-${periodo}.csv`));
  }
  private guardar(blob: Blob, nombre: string): void {
    const url = URL.createObjectURL(blob); const enlace = document.createElement('a');
    enlace.href = url; enlace.download = nombre; enlace.click(); URL.revokeObjectURL(url);
  }
  private error(err: any): void { this.mensaje.set(err?.error?.message || 'No se pudo completar la operación.'); }
}
