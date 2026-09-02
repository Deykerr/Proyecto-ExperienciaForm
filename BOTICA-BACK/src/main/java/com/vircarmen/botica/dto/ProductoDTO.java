package com.vircarmen.botica.dto;
import java.math.BigDecimal;

public record ProductoDTO(
        Integer idProducto,
        String nombre,
        String descripcion, // <--- Agregado
        String codigoBarras,
        BigDecimal precioVenta,
        Integer stockActual,
        Integer stockMinimo,
        Boolean activo,
        String nombreCategoria,
        Integer unidadesPorPresentacion,
        BigDecimal precioPresentacion
) {}