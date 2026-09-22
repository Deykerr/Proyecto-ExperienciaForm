package com.vircarmen.botica.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.entity.Comprobante;
import com.vircarmen.botica.entity.SerieComprobante;
import com.vircarmen.botica.entity.TipoComprobante;
import com.vircarmen.botica.entity.Venta;
import com.vircarmen.botica.repository.ComprobanteRepository;
import com.vircarmen.botica.repository.SerieComprobanteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComprobanteService {
    private final ComprobanteRepository comprobanteRepository;
    private final SerieComprobanteRepository serieRepository;

    @Transactional
    public Comprobante generarComprobante(Venta venta, TipoComprobante tipo) {
        String codigoSerie = switch (tipo) {
            case FACTURA -> "F001";
            case BOLETA -> "B001";
            case TICKET -> "T001";
        };

        SerieComprobante serie = serieRepository.findByTipoComprobanteAndSerie(tipo, codigoSerie)
                .orElseGet(() -> {
                    SerieComprobante nueva = new SerieComprobante();
                    nueva.setTipoComprobante(tipo);
                    nueva.setSerie(codigoSerie);
                    nueva.setUltimoCorrelativo(0L);
                    return serieRepository.saveAndFlush(nueva);
                });
        serie.setUltimoCorrelativo(serie.getUltimoCorrelativo() + 1);
        serieRepository.save(serie);

        Comprobante comprobante = new Comprobante();
        comprobante.setVenta(venta);
        comprobante.setTipoComprobante(tipo);
        comprobante.setSerie(codigoSerie);
        comprobante.setCorrelativo(String.format("%08d", serie.getUltimoCorrelativo()));
        comprobante.setCliente(venta.getCliente());
        comprobante.setSubtotal(venta.getSubtotal());
        comprobante.setIgv(venta.getIgv());
        comprobante.setTotal(venta.getTotal());
        comprobante.setFechaEmision(LocalDateTime.now());
        return comprobanteRepository.save(comprobante);
    }
}
