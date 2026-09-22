import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ClienteDTO, DocumentoElectronicoDTO, ProductoDTO, RecetaDTO,
  ReporteDigemidDTO, ResumenDiarioSunatDTO, VentaResumenDTO
} from '../models';

@Injectable({ providedIn: 'root' })
export class OperacionesFarmaciaService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  listarVentas(): Observable<VentaResumenDTO[]> {
    return this.http.get<VentaResumenDTO[]>(`${this.api}/ventas`);
  }

  devolverVenta(idVenta: number, request: unknown): Observable<unknown> {
    return this.http.post(`${this.api}/ventas/${idVenta}/devoluciones`, request);
  }

  anularVenta(idVenta: number, request: unknown): Observable<unknown> {
    return this.http.post(`${this.api}/ventas/${idVenta}/anular`, request);
  }

  listarRecetas(): Observable<RecetaDTO[]> {
    return this.http.get<RecetaDTO[]>(`${this.api}/recetas`);
  }

  crearReceta(request: unknown): Observable<RecetaDTO> {
    return this.http.post<RecetaDTO>(`${this.api}/recetas`, request);
  }

  listarClientes(): Observable<ClienteDTO[]> {
    return this.http.get<ClienteDTO[]>(`${this.api}/clientes`);
  }

  buscarProductos(termino: string): Observable<ProductoDTO[]> {
    const params = new HttpParams().set('termino', termino).set('page', 0).set('size', 20);
    return this.http.get<any>(`${this.api}/productos/buscar`, { params })
      .pipe(map(response => response?.content ?? []));
  }

  listarDocumentosSunat(): Observable<DocumentoElectronicoDTO[]> {
    return this.http.get<DocumentoElectronicoDTO[]>(`${this.api}/sunat/documentos`);
  }

  procesarDocumentoSunat(id: number): Observable<DocumentoElectronicoDTO> {
    return this.http.post<DocumentoElectronicoDTO>(`${this.api}/sunat/documentos/${id}/procesar`, {});
  }

  descargarXmlSunat(id: number): Observable<Blob> {
    return this.http.get(`${this.api}/sunat/documentos/${id}/xml`, { responseType: 'blob' });
  }

  descargarCdrSunat(id: number): Observable<Blob> {
    return this.http.get(`${this.api}/sunat/documentos/${id}/cdr`, { responseType: 'blob' });
  }

  listarResumenesSunat(): Observable<ResumenDiarioSunatDTO[]> {
    return this.http.get<ResumenDiarioSunatDTO[]>(`${this.api}/sunat/resumenes-diarios`);
  }

  crearResumenSunat(): Observable<ResumenDiarioSunatDTO> {
    return this.http.post<ResumenDiarioSunatDTO>(`${this.api}/sunat/resumenes-diarios`, {});
  }

  procesarResumenSunat(id: number): Observable<ResumenDiarioSunatDTO> {
    return this.http.post<ResumenDiarioSunatDTO>(`${this.api}/sunat/resumenes-diarios/${id}/procesar`, {});
  }

  descargarXmlResumenSunat(id: number): Observable<Blob> {
    return this.http.get(`${this.api}/sunat/resumenes-diarios/${id}/xml`, { responseType: 'blob' });
  }

  descargarCdrResumenSunat(id: number): Observable<Blob> {
    return this.http.get(`${this.api}/sunat/resumenes-diarios/${id}/cdr`, { responseType: 'blob' });
  }

  listarReportesDigemid(): Observable<ReporteDigemidDTO[]> {
    return this.http.get<ReporteDigemidDTO[]>(`${this.api}/digemid/reportes-precios`);
  }

  generarReporteDigemid(periodo: string): Observable<ReporteDigemidDTO> {
    return this.http.post<ReporteDigemidDTO>(`${this.api}/digemid/reportes-precios`, {}, { params: { periodo } });
  }

  registrarResultadoDigemid(id: number, request: unknown): Observable<ReporteDigemidDTO> {
    return this.http.post<ReporteDigemidDTO>(`${this.api}/digemid/reportes-precios/${id}/resultado`, request);
  }

  descargarReporteDigemid(id: number): Observable<Blob> {
    return this.http.get(`${this.api}/digemid/reportes-precios/${id}/archivo`, { responseType: 'blob' });
  }
}
