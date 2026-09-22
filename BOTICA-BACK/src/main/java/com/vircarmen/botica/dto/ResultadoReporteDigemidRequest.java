package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResultadoReporteDigemidRequest(
        @NotBlank String estado,
        @Size(max = 100) String constancia,
        @Size(max = 1000) String observaciones) {}
