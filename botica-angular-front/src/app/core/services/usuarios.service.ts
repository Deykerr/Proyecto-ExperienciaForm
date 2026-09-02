import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UsuarioDTO } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class UsuariosService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/usuarios`;

    listarUsuarios(): Observable<UsuarioDTO[]> {
        return this.http.get<UsuarioDTO[]>(this.apiUrl);
    }

    // Aquí enviamos un objeto que incluya el password para la creación
    crearUsuario(usuario: any): Observable<any> {
        return this.http.post(this.apiUrl, usuario);
    }
}