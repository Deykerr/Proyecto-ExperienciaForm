package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RecepcionCompraDTO(
        Integer idRecepcion, Integer idCompra, String documentoProveedor,
        LocalDateTime fecha, String estado, String observaciones, List<Item> detalles) {
    public record Item(Integer idDetalleCompra, Integer idProducto, String producto,
                       Integer idLote, String lote, Integer cantidad, BigDecimal costoUnitario) {}
}
