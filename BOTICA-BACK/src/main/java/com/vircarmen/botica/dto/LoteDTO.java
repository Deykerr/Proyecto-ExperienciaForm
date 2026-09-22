package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteDTO(
        Integer idLote,
        String codigoLote,
        LocalDate fechaIngreso,
        LocalDate fechaVencimiento,
        Integer stockInicial,
        Integer stockActual,
        BigDecimal precioCompra,
        String estado,
        Integer idProducto,
        String producto,
        String codigoBarras) {
}
