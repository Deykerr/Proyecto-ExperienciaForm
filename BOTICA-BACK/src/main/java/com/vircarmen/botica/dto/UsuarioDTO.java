package com.vircarmen.botica.dto;

import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.Rol;

public record UsuarioDTO(
        Integer idUsuario,
        String nombreCompleto,
        String username,
        Rol rol,
        EstadoGeneral estado
) {}