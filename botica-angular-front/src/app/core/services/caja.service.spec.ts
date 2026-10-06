import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CajaService } from './caja.service';

describe('CajaService', () => {
  let service: CajaService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CajaService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CajaService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('envía los filtros definidos al historial', () => {
    service.listarHistorial({
      desde: '2026-10-01',
      hasta: '2026-10-05',
      estado: 'CERRADA',
      usuario: 'cajero'
    }).subscribe(cajas => expect(cajas).toEqual([]));

    const request = httpTesting.expectOne(req => req.url === `${environment.apiUrl}/caja/historial`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('desde')).toBe('2026-10-01');
    expect(request.request.params.get('hasta')).toBe('2026-10-05');
    expect(request.request.params.get('estado')).toBe('CERRADA');
    expect(request.request.params.get('usuario')).toBe('cajero');
    request.flush([]);
  });

  it('consulta el detalle de una caja específica', () => {
    service.obtenerDetalleHistorial(17).subscribe(detalle => {
      expect(detalle.caja.idCajaSesion).toBe(17);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/caja/historial/17`);
    expect(request.request.method).toBe('GET');
    request.flush({
      caja: { idCajaSesion: 17, usuario: 'admin', fechaApertura: '2026-10-05T09:00:00', montoInicial: 100, estado: 'ABIERTA' },
      movimientos: []
    });
  });
});
