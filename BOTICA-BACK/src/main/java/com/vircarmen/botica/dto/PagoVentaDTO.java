package com.vircarmen.botica.dto;

import java.math.BigDecimal;

public record PagoVentaDTO(
    String metodo, // "EFECTIVO", "YAPE", "PLIN", "TARJETA"
    BigDecimal monto
) {}