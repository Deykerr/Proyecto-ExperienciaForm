package com.vircarmen.botica.dto;

import java.math.BigDecimal;

public record MetricasDTO(
    BigDecimal totalVentas,
    Integer cantidadOperaciones,
    BigDecimal ticketPromedio
) {}
