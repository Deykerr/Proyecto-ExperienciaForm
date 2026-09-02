package com.vircarmen.botica.dto;

public record BajoStockDTO(
    String codigoBarras,
    String nombre,
    Integer stockActual,
    Integer stockMinimo
) {}
