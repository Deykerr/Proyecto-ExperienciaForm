package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResumenDTO(
        Integer idVenta, LocalDateTime fechaEmision, String cliente, String estado,
        BigDecimal subtotal, BigDecimal igv, BigDecimal total,
        String tipoComprobante, String numeroComprobante,
        Integer idReceta, String numeroReceta, List<Item> detalles) {
    public record Item(Integer idDetalleVenta, Integer idProducto, String producto,
                       Integer idLote, String lote, Integer cantidad,
                       Integer cantidadDevuelta, Integer cantidadDisponibleDevolucion,
                       BigDecimal subtotal) {}
}
