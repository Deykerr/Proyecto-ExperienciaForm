package com.vircarmen.botica.dto;

import java.time.LocalDateTime;

public record DocumentoElectronicoDTO(
        Integer idDocumento, Integer idComprobante, Integer idDevolucionVenta,
        String tipoDocumento, String serie, String correlativo,
        LocalDateTime fechaEmision, String estado, String hashXml,
        String codigoRespuesta, String descripcionRespuesta, Integer intentos,
        LocalDateTime ultimoIntento, LocalDateTime siguienteIntento) {}
