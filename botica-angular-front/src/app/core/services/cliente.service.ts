import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClienteDTO, ClienteRequest } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/clientes`;

  listarClientes(): Observable<ClienteDTO[]> {
    return this.http.get<ClienteDTO[]>(this.apiUrl);
  }

  crearCliente(cliente: ClienteRequest): Observable<ClienteDTO> {
    return this.http.post<ClienteDTO>(this.apiUrl, cliente);
  }

  buscarPorDocumento(documento: string): Observable<ClienteDTO> {
    return this.http.get<ClienteDTO>(`${this.apiUrl}/${documento}`);
  }

  buscarPorCriterio(criterio: string, termino: string): Observable<ClienteDTO[]> {
    return this.http.get<ClienteDTO[]>(`${this.apiUrl}/buscar?criterio=${criterio}&termino=${termino}`);
  }
}