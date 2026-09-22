package com.vircarmen.botica.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DevolucionVentaRequest(
        @NotBlank @Size(max = 64) String idempotencyKey,
        @NotBlank @Size(max = 300) String motivo,
        String metodoReembolso,
        @Size(max = 100) String referenciaReembolso,
        @Valid List<Reembolso> reembolsos,
        @NotEmpty @Valid List<Item> items) {
    public record Reembolso(
            @NotBlank String metodoPago,
            @NotNull @Positive java.math.BigDecimal monto,
            @Size(max = 100) String referencia) {}
    public record Item(
            @NotNull Integer idDetalleVenta,
            @NotNull @Positive Integer cantidad) {}
}
