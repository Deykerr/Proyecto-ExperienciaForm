package com.vircarmen.botica.service;

import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.Rol;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.security.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;

    @Transactional
    public Usuario registrarEmpleado(String username, String passwordPlano, String nombreCompleto, Rol rol) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new BusinessException("El nombre de usuario ya está en uso");
        }
        passwordPolicyService.validar(passwordPlano);

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setNombreCompleto(nombreCompleto);
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(passwordPlano));

        return usuarioRepository.save(nuevoUsuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Integer idUsuario) {
        return usuarioRepository.findById(Integer.valueOf(idUsuario))
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    @Transactional
    public Usuario actualizarUsuario(Integer idUsuario, String nombreCompleto, String passwordPlano, Rol rol) {
        Usuario usuario = buscarPorId(idUsuario);
        usuario.setNombreCompleto(nombreCompleto);
        if (passwordPlano != null && !passwordPlano.isBlank()) {
            passwordPolicyService.validar(passwordPlano);
            usuario.setPasswordHash(passwordEncoder.encode(passwordPlano));
        }
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario cambiarEstadoUsuario(Integer idUsuario, EstadoGeneral estado) {
        Usuario usuario = buscarPorId(idUsuario);
        usuario.setEstado(estado);
        return usuarioRepository.save(usuario);
    }
}
