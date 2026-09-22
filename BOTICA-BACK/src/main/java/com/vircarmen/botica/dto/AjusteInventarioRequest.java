package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AjusteInventarioRequest(
        @NotNull(message = "El lote es obligatorio")
        Integer idLote,

        @NotNull(message = "La cantidad contada es obligatoria")
        @PositiveOrZero(message = "La cantidad contada no puede ser negativa")
        Integer cantidadContada,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 300, message = "El motivo no puede superar 300 caracteres")
        String motivo
) {}
