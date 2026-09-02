package com.vircarmen.botica.dto;
import java.math.BigDecimal;

public record ProductoRequest(
        String nombre,
        String codigoBarras,
        String codigoSunat,
        String tipoAfectacionIgv,
        BigDecimal precioCompra,
        BigDecimal precioVenta,
        Integer stockMinimo,
        Integer idCategoria,
        Integer unidadesPorPresentacion,
        BigDecimal precioPresentacion
) {}