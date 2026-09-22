package com.vircarmen.botica.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ResumenAlertasInventarioDTO(
        int total,
        int criticas,
        int advertencias,
        int agotados,
        int bajoStock,
        int vencidosConStock,
        int proximosAVencer,
        int diasVencimientoConsultados,
        LocalDateTime generadoEn,
        List<AlertaInventarioDTO> alertas) {
}
