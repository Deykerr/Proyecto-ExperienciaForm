package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record IngresoInventarioRequest(
        @NotNull(message = "El producto es obligatorio") Integer idProducto,
        @NotBlank(message = "El código de lote es obligatorio") @Size(max = 50) String codigoLote,
        @NotNull(message = "La fecha de vencimiento es obligatoria") @Future LocalDate fechaVencimiento,
        @NotNull(message = "La cantidad es obligatoria") @Positive Integer cantidad,
        @NotNull(message = "El costo unitario es obligatorio") @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2) BigDecimal costoUnitario,
        @NotBlank(message = "El motivo es obligatorio") @Size(max = 300) String motivo,
        @NotBlank(message = "La clave de idempotencia es obligatoria") @Size(max = 64) String idempotencyKey
) {}
