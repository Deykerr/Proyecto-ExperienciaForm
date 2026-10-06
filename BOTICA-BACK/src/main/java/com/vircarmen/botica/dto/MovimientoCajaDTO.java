package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoCajaDTO(
        Integer idMovimiento,
        String tipo,
        BigDecimal monto,
        String motivo,
        LocalDateTime fecha,
        String usuario,
        String origen,
        Integer referenciaId
) {}
