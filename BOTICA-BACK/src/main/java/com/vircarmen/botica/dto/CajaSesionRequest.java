package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record CajaSesionRequest(
    @NotNull(message = "El monto inicial es obligatorio")
    @Min(value = 0, message = "El monto inicial no puede ser negativo")
    BigDecimal montoInicial
) {}
