package com.vircarmen.botica.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ConteoFisicoDTO(
        Integer idConteo,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFinalizacion,
        String motivo,
        String estado,
        String usuario,
        List<Item> items
) {
    public record Item(
            Integer idLote,
            String codigoLote,
            String producto,
            Integer stockSistema,
            Integer stockContado,
            Integer diferencia
    ) {}
}
