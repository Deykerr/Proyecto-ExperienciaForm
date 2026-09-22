package com.vircarmen.botica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vircarmen.botica.dto.PagoRequest;
import com.vircarmen.botica.entity.CajaSesion;
import com.vircarmen.botica.entity.MetodoPago;
import com.vircarmen.botica.entity.Pago;
import com.vircarmen.botica.entity.Venta;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.PagoRepository;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private PagoService pagoService;

    @Test
    void efectivoRegistraMontoAplicadoMontoRecibidoYVuelto() {
        Venta venta = venta("80.00");
        CajaSesion caja = cajaAbierta();
        PagoRequest request = pago("EFECTIVO", "100.00", null);

        BigDecimal vuelto = pagoService.procesarPagos(venta, List.of(request), caja);

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(captor.capture());
        Pago guardado = captor.getValue();
        assertEquals(new BigDecimal("20.00"), vuelto);
        assertEquals(new BigDecimal("80.00"), guardado.getMonto());
        assertEquals(new BigDecimal("100.00"), guardado.getMontoRecibido());
        assertEquals(new BigDecimal("20.00"), guardado.getVuelto());
        assertEquals(MetodoPago.EFECTIVO, guardado.getMetodoPago());
    }

    @Test
    void pagoElectronicoExigeReferencia() {
        Venta venta = venta("50.00");

        assertThrows(BusinessException.class,
                () -> pagoService.procesarPagos(venta, List.of(pago("YAPE", "50.00", " ")), cajaAbierta()));
    }

    private Venta venta(String total) {
        Venta venta = new Venta();
        venta.setTotal(new BigDecimal(total));
        return venta;
    }

    private CajaSesion cajaAbierta() {
        CajaSesion caja = new CajaSesion();
        caja.setEstado(CajaSesion.EstadoCaja.ABIERTA);
        return caja;
    }

    private PagoRequest pago(String metodo, String monto, String referencia) {
        PagoRequest request = new PagoRequest();
        request.setMetodoPago(metodo);
        request.setMontoRecibido(new BigDecimal(monto));
        request.setReferencia(referencia);
        return request;
    }
}
