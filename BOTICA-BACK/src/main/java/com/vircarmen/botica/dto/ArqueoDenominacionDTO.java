package com.vircarmen.botica.dto;

import java.math.BigDecimal;

public record ArqueoDenominacionDTO(
        BigDecimal denominacion,
        Integer cantidad,
        BigDecimal subtotal
) {}
