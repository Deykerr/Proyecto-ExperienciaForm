package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotBlank;

public record LaboratorioRequest(
    @NotBlank(message = "El nombre del laboratorio es obligatorio")
    String nombre,
    String descripcion
) {}
