package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
    @NotBlank @Size(max = 20) String tipoDocumento,
    @NotBlank @Size(max = 15) String numeroDocumento,
    @NotBlank @Size(min = 3, max = 200) String nombreRazonSocial,
    @Size(max = 255) String direccion
) {}
