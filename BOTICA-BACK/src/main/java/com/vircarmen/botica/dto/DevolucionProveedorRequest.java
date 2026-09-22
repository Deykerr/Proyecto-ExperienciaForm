package com.vircarmen.botica.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DevolucionProveedorRequest(
        @NotBlank @Size(max = 64) String idempotencyKey,
        @NotBlank @Size(max = 300) String motivo,
        @Size(max = 60) String documentoReferencia,
        @NotEmpty @Valid List<Item> detalles) {
    public record Item(
            @NotNull Integer idDetalleCompra,
            @NotNull Integer idLote,
            @NotNull @Positive Integer cantidad) {}
}
