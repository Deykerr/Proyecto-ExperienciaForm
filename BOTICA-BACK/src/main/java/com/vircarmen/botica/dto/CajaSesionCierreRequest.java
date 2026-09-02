package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record CajaSesionCierreRequest(
    @NotNull(message = "El monto final es obligatorio")
    @Min(value = 0, message = "El monto final no puede ser negativo")
    BigDecimal montoFinal
) {}
