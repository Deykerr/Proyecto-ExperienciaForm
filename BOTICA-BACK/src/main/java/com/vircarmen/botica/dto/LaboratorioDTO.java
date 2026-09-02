package com.vircarmen.botica.dto;

public record LaboratorioDTO(
    Integer idLaboratorio,
    String nombre,
    String descripcion,
    String estado
) {}
