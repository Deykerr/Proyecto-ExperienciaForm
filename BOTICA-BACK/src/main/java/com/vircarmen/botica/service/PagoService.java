package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.PagoRequest;
import com.vircarmen.botica.entity.*;
import com.vircarmen.botica.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {
    private final PagoRepository pagoRepository;

    public void procesarPagos(Venta venta, List<PagoRequest> peticionPagos, CajaSesion caja) {
        if (peticionPagos == null || peticionPagos.isEmpty()) {
            throw new com.vircarmen.botica.exception.BusinessException("Debe ingresar al menos un mtodo de pago.");
        }
        
        BigDecimal sumaPagos = BigDecimal.ZERO;
        for (PagoRequest pReq : peticionPagos) {
            if (pReq.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                throw new com.vircarmen.botica.exception.BusinessException("El monto del pago debe ser mayor a cero.");
            }
            sumaPagos = sumaPagos.add(pReq.getMonto());
        }

        if (sumaPagos.compareTo(venta.getTotal()) < 0) {
            throw new com.vircarmen.botica.exception.BusinessException("El total pagado es menor al total de la venta.");
        }

        for (PagoRequest pReq : peticionPagos) {
            Pago pago = new Pago();
            pago.setVenta(venta);
            pago.setMetodoPago(MetodoPago.valueOf(pReq.getMetodoPago()));
            pago.setMonto(pReq.getMonto());
            pago.setCajaSesion(caja);
            pago.setEstado(EstadoPago.APROBADO);
            pagoRepository.save(pago);
            venta.getPagos().add(pago);
        }
    }
}
