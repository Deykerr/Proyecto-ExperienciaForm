package com.vircarmen.botica.dto;

public record ProveedorDTO(
    Integer idProveedor,
    String ruc,
    String razonSocial,
    String telefono,
    String correo,
    String direccion,
    String estado
) {
}
