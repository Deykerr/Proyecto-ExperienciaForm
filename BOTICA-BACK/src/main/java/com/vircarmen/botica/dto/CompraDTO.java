package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CompraDTO(
        Integer idCompra, Integer idProveedor, String proveedor, String documento,
        LocalDateTime fechaCompra, LocalDate fechaEsperada, String observaciones,
        BigDecimal total, String estado, List<Item> detalles) {
    public record Item(Integer idDetalleCompra, Integer idProducto, String producto,
                       Integer cantidad, Integer cantidadRecibida, Integer cantidadPendiente,
                       BigDecimal costoUnitario, BigDecimal subtotal) {}
}
