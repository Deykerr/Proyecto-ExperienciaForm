package com.vircarmen.botica.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ConteoFisicoRequest(
        @NotBlank(message = "El motivo es obligatorio") @Size(max = 300) String motivo,
        @NotEmpty(message = "Debe ingresar al menos un lote contado") @Valid List<Item> items
) {
    public record Item(
            @NotNull(message = "El lote es obligatorio") Integer idLote,
            @NotNull(message = "La cantidad contada es obligatoria") @PositiveOrZero Integer cantidadContada
    ) {}
}
