package com.vircarmen.botica.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vircarmen.botica.entity.Usuario;

public class SecurityUtils {

    private SecurityUtils() {
        // Ocultar el constructor público implícito
    }

    public static Integer getUsuarioAutenticadoId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Acceso denegado: No hay usuario autenticado en el contexto.");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Usuario) {
            return ((Usuario) principal).getIdUsuario();
        }

        throw new IllegalStateException("El principal de seguridad no es una instancia de Usuario.");
    }
}
