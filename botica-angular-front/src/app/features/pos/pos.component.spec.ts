import { TestBed } from '@angular/core/testing';

import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { TicketService } from '../../core/services/ticket.service';
import { VentasService } from '../../core/services/venta.service';
import { PosComponent } from './pos.component';

describe('PosComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PosComponent],
      providers: [
        { provide: VentasService, useValue: jasmine.createSpyObj('VentasService', ['listarProductosDestacados']) },
        { provide: AuthService, useValue: jasmine.createSpyObj('AuthService', ['currentUser']) },
        { provide: TicketService, useValue: jasmine.createSpyObj('TicketService', ['imprimirTicket']) },
        { provide: AlertService, useValue: jasmine.createSpyObj('AlertService', ['success', 'error', 'warning', 'confirm']) }
      ]
    }).compileComponents();
  });

  it('cierra y limpia el proceso de pago después de finalizar la venta', () => {
    const fixture = TestBed.createComponent(PosComponent);
    const component = fixture.componentInstance;
    component.mostrarModalPago.set(true);
    component.metodoPagoSeleccionado.set('YAPE');
    component.montoPagoInput.set(25);
    component.pagosAgregados.set([{ metodoPago: 'YAPE', montoRecibido: 25, referencia: 'PRUEBA' }]);

    component.limpiarPos();

    expect(component.mostrarModalPago()).toBeFalse();
    expect(component.pagosAgregados()).toEqual([]);
    expect(component.metodoPagoSeleccionado()).toBe('EFECTIVO');
    expect(component.montoPagoInput()).toBe(0);
  });
});
