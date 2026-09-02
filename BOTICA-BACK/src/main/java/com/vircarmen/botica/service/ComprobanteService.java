package com.vircarmen.botica.service;

import com.vircarmen.botica.entity.*;
import com.vircarmen.botica.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ComprobanteService {
    private final ComprobanteRepository comprobanteRepository;

    public Comprobante generarComprobante(Venta venta, String tipoDoc) {
        Comprobante comp = new Comprobante();
        comp.setVenta(venta);
        comp.setTipoComprobante(TipoComprobante.valueOf(tipoDoc));
        
        comp.setSerie(comp.getTipoComprobante() == TipoComprobante.FACTURA ? "F001" : "B001");
        long count = comprobanteRepository.count();
        comp.setCorrelativo(String.format("%08d", count + 1));
        
        comp.setCliente(venta.getCliente());
        comp.setSubtotal(venta.getSubtotal());
        comp.setIgv(venta.getIgv());
        comp.setTotal(venta.getTotal());
        comp.setFechaEmision(LocalDateTime.now());
        
        return comprobanteRepository.save(comp);
    }
}
