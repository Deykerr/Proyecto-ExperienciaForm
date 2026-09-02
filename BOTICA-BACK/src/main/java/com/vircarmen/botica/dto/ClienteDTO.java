package com.vircarmen.botica.dto;

public record ClienteDTO(
    Integer idCliente,
    String tipoDocumento,
    String numeroDocumento,
    String nombreRazonSocial
) {}
