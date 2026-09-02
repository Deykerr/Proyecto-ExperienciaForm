package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotBlank;

public record ProveedorRequest(
    @NotBlank(message = "El RUC es obligatorio")
    String ruc,
    
    @NotBlank(message = "La razón social es obligatoria")
    String razonSocial,
    
    String telefono,
    String correo,
    String direccion
) {
}
