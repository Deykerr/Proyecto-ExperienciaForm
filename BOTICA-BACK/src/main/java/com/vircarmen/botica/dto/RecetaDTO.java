package com.vircarmen.botica.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RecetaDTO(
        Integer idReceta,
        String numero,
        Integer idCliente,
        String pacienteNombre,
        String pacienteDocumento,
        String medicoNombre,
        String medicoColegiatura,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        String condicionVenta,
        String codigoCondicionVentaDigemid,
        String estado,
        LocalDateTime retenidaEn,
        String observaciones,
        List<Item> detalles) {
    public record Item(Integer idRecetaDetalle, Integer idProducto, String producto,
                       Integer cantidadAutorizada, Integer cantidadDispensada,
                       Integer cantidadDisponible, String indicaciones) {}
}
