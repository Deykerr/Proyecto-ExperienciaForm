package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DetalleVentaDTO(
    @NotNull(message = "El producto es obligatorio")
    Integer idProducto,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    Integer cantidad
) {}
