package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecepcionCompraRequest(
        @NotBlank @Size(max = 64) String idempotencyKey,
        @Size(max = 60) String documentoProveedor,
        @Size(max = 500) String observaciones,
        @NotEmpty @Valid List<Item> detalles) {
    public record Item(
            @NotNull Integer idDetalleCompra,
            @NotNull @Positive Integer cantidad,
            @NotBlank @Size(max = 50) String codigoLote,
            @NotNull @Future LocalDate fechaVencimiento,
            @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal costoUnitario) {}
}
