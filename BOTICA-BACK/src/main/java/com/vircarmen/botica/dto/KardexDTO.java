package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KardexDTO(
        Integer idDetalle,
        LocalDateTime fecha,
        String tipoMovimiento,
        Integer idProducto,
        String producto,
        Integer idLote,
        String codigoLote,
        Integer cantidad,
        Integer stockLoteAnterior,
        Integer stockLotePosterior,
        Integer stockProductoAnterior,
        Integer stockProductoPosterior,
        BigDecimal precioUnitario,
        String referenciaTipo,
        Integer referenciaId,
        String motivo,
        String usuario
) {}
