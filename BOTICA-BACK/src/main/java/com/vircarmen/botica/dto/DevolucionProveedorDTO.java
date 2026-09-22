package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DevolucionProveedorDTO(
        Integer idDevolucionProveedor, Integer idCompra, Integer idProveedor,
        LocalDateTime fecha, String motivo, String documentoReferencia,
        String estado, BigDecimal total, List<Item> detalles) {
    public record Item(Integer idDetalleCompra, Integer idProducto, String producto,
                       Integer idLote, String lote, Integer cantidad,
                       BigDecimal costoUnitario, BigDecimal subtotal) {}
}
