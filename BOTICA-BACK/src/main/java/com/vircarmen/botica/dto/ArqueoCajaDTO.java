package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ArqueoCajaDTO(
        Integer idArqueo,
        BigDecimal totalContado,
        BigDecimal saldoEsperado,
        BigDecimal diferencia,
        String estado,
        String observaciones,
        LocalDateTime fecha,
        String aprobadoPor,
        LocalDateTime fechaAprobacion,
        String observacionAprobacion,
        List<ArqueoDenominacionDTO> denominaciones
) {}
