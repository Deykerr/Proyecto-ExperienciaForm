package com.vircarmen.botica.dto;

public record ConciliacionStockDTO(
        Integer idProducto,
        String producto,
        Integer stockRegistrado,
        Integer stockCalculado,
        Integer diferencia
) {}
