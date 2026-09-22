package com.vircarmen.botica.dto;

public record AuthResponse(
        String username,
        Integer idUsuario,
        String nombreCompleto,
        String rol
) {}
