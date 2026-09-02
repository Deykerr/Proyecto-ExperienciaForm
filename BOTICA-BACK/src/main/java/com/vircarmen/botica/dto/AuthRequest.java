package com.vircarmen.botica.dto;


import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "El usuario no puede estar vacío") 
        String username,
        
        @NotBlank(message = "La contraseña no puede estar vacía") 
        String password
) {}