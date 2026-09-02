package com.vircarmen.botica.dto;

public record LoteVencerDTO(
    String codigoLote,
    String producto,
    String fechaVencimiento,
    Integer cantidad
) {}
