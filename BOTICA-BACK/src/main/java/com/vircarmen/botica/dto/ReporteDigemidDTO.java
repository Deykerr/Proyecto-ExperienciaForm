package com.vircarmen.botica.dto;

import java.time.LocalDateTime;

public record ReporteDigemidDTO(
        Integer idReporte, String periodo, LocalDateTime fechaGeneracion,
        String estado, Integer cantidadProductos, String hashArchivo,
        LocalDateTime fechaEnvio, String constancia, String observaciones) {}
