package com.vircarmen.botica.dto;

import java.time.LocalDate;

public record AlertaInventarioDTO(
        String idAlerta,
        String tipo,
        String severidad,
        Integer idProducto,
        String producto,
        String codigoBarras,
        Integer stockActual,
        Integer stockMinimo,
        Integer cantidadSugeridaReposicion,
        Integer idLote,
        String codigoLote,
        LocalDate fechaVencimiento,
        Long diasParaVencer,
        String mensaje,
        String accionSugerida) {
}
