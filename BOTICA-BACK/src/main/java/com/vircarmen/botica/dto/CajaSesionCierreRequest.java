package com.vircarmen.botica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CajaSesionCierreRequest(
    @DecimalMin(value = "0.00", message = "El monto final no puede ser negativo")
    @Digits(integer = 8, fraction = 2) BigDecimal montoFinal,
    @Size(max = 500) String observaciones,
    @Valid List<Denominacion> denominaciones
) {
    public record Denominacion(
            @DecimalMin("0.01") @Digits(integer = 6, fraction = 2) BigDecimal denominacion,
            @PositiveOrZero Integer cantidad) {}
}
