package com.vircarmen.botica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vircarmen.botica.entity.Rol;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.PasswordPolicyService;

@Configuration
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
public class InitialAdminProvisioner {
    private static final Logger LOG = LoggerFactory.getLogger(InitialAdminProvisioner.class);

    @Bean
    CommandLineRunner provisionarAdministradorInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyService passwordPolicyService,
            @Value("${app.bootstrap-admin.username:}") String username,
            @Value("${app.bootstrap-admin.password:}") String password,
            @Value("${app.bootstrap-admin.full-name:Administrador inicial}") String nombreCompleto) {
        return args -> {
            if (usuarioRepository.count() > 0) {
                LOG.info("El aprovisionamiento inicial no se ejecutó porque ya existen usuarios.");
                return;
            }
            if (username.isBlank() || password.isBlank() || nombreCompleto.isBlank()) {
                throw new IllegalStateException(
                        "BOOTSTRAP_ADMIN_USERNAME, BOOTSTRAP_ADMIN_PASSWORD y BOOTSTRAP_ADMIN_FULL_NAME son obligatorios");
            }
            passwordPolicyService.validar(password);

            Usuario admin = new Usuario();
            admin.setUsername(username.trim());
            admin.setNombreCompleto(nombreCompleto.trim());
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRol(Rol.ADMIN);
            usuarioRepository.save(admin);
            LOG.warn("Administrador inicial creado. Desactiva BOOTSTRAP_ADMIN_ENABLED y elimina su contraseña del entorno.");
        };
    }
}
