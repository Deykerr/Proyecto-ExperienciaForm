package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DevolucionVentaDTO(
        Integer idDevolucionVenta,
        Integer idVenta,
        LocalDateTime fecha,
        String tipo,
        String estado,
        String motivo,
        String metodoReembolso,
        String referenciaReembolso,
        BigDecimal subtotal,
        BigDecimal igv,
        BigDecimal total,
        List<Reembolso> reembolsos,
        List<Item> detalles) {
    public record Reembolso(String metodoPago, BigDecimal monto, String referencia) {}
    public record Item(Integer idDetalleVenta, Integer idProducto, String producto,
                       Integer idLote, String lote, Integer cantidad, BigDecimal subtotal) {}
}
