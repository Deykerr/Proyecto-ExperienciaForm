package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArqueoAprobacionRequest(@NotBlank @Size(max = 500) String observacion) {}
