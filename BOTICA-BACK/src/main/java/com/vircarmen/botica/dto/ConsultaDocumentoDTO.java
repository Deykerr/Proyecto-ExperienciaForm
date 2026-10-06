package com.vircarmen.botica.dto;

public record ConsultaDocumentoDTO(
        Integer idCliente,
        String tipoDocumento,
        String numeroDocumento,
        String nombreRazonSocial,
        String direccion,
        boolean encontrado,
        boolean requiereRegistro,
        String origen,
        String mensaje
) {}
