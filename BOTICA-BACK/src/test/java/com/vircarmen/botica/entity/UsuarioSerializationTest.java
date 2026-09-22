package com.vircarmen.botica.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class UsuarioSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void noExponeHashNiPropiedadesDeSeguridad() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setUsername("admin");
        usuario.setNombreCompleto("Administrador");
        usuario.setPasswordHash("hash-super-secreto");
        usuario.setRol(Rol.ADMIN);

        String json = objectMapper.writeValueAsString(usuario);

        assertFalse(json.contains("hash-super-secreto"));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("authorities"));
    }
}
