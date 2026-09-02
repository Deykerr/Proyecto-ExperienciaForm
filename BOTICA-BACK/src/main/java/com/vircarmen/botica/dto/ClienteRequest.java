package com.vircarmen.botica.dto;

public record ClienteRequest(
    String tipoDocumento,
    String numeroDocumento,
    String nombreRazonSocial,
    String direccion
) {}
