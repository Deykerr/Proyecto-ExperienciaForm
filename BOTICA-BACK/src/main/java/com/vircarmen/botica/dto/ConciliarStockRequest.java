package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConciliarStockRequest(
        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 300, message = "El motivo no puede superar 300 caracteres")
        String motivo
) {}
