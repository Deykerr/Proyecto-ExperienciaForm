package com.vircarmen.botica.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResumenDiarioSunatDTO(
        Integer idResumen,
        String identificador,
        LocalDate fechaReferencia,
        LocalDateTime fechaGeneracion,
        String estado,
        String hashXml,
        String ticket,
        String codigoRespuesta,
        String descripcionRespuesta,
        Integer intentos,
        Integer cantidadDocumentos) {}
