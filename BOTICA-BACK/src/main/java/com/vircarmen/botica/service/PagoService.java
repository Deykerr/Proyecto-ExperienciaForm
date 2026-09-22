package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.PagoRequest;
import com.vircarmen.botica.entity.CajaSesion;
import com.vircarmen.botica.entity.EstadoPago;
import com.vircarmen.botica.entity.MetodoPago;
import com.vircarmen.botica.entity.Pago;
import com.vircarmen.botica.entity.Venta;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.PagoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagoService {
    private final PagoRepository pagoRepository;

    @Transactional
    public BigDecimal procesarPagos(Venta venta, List<PagoRequest> peticionPagos, CajaSesion caja) {
        if (caja.getEstado() != CajaSesion.EstadoCaja.ABIERTA) {
            throw new BusinessException("La caja está cerrada.");
        }
        if (peticionPagos == null || peticionPagos.isEmpty()) {
            throw new BusinessException("Debe ingresar al menos un método de pago.");
        }

        BigDecimal saldo = venta.getTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal vueltoTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (PagoRequest request : peticionPagos) {
            if (saldo.compareTo(BigDecimal.ZERO) == 0) {
                throw new BusinessException("Se enviaron pagos adicionales después de cubrir la venta.");
            }

            MetodoPago metodo = parseMetodo(request.getMetodoPago());
            BigDecimal recibido = request.getMontoRecibido().setScale(2, RoundingMode.HALF_UP);
            if (recibido.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("El monto recibido debe ser mayor a cero.");
            }
            if (metodo != MetodoPago.EFECTIVO && isBlank(request.getReferencia())) {
                throw new BusinessException("La referencia es obligatoria para pagos no efectivos.");
            }

            BigDecimal aplicado = recibido.min(saldo);
            BigDecimal vuelto = recibido.subtract(aplicado);
            if (metodo != MetodoPago.EFECTIVO && vuelto.compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException("Un pago no efectivo no puede superar el saldo pendiente.");
            }

            Pago pago = new Pago();
            pago.setVenta(venta);
            pago.setMetodoPago(metodo);
            pago.setMonto(aplicado);
            pago.setMontoRecibido(recibido);
            pago.setVuelto(vuelto);
            pago.setReferencia(isBlank(request.getReferencia()) ? null : request.getReferencia().trim());
            pago.setCajaSesion(caja);
            pago.setEstado(EstadoPago.APROBADO);
            pagoRepository.save(pago);
            venta.getPagos().add(pago);

            saldo = saldo.subtract(aplicado);
            vueltoTotal = vueltoTotal.add(vuelto);
        }

        if (saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("El total aplicado de los pagos es menor al total de la venta.");
        }
        return vueltoTotal;
    }

    private MetodoPago parseMetodo(String valor) {
        try {
            return MetodoPago.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException("Método de pago no válido: " + valor);
        }
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }
}
