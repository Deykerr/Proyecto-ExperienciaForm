package com.vircarmen.botica.service;

import java.util.HashMap;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.vircarmen.botica.dto.AuthRequest;
import com.vircarmen.botica.dto.AuthResponse;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UserDetailsService userDetailsService;

    public AuthResponse login(AuthRequest request) {
        // 1. Spring Security verifica las credenciales y el password encriptado
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. Si es exitoso, traemos los datos completos de tu base de datos
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en base de datos"));
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        // 3. Generamos el Token JWT firmado
        String token = jwtService.generateToken(new HashMap<>(), userDetails);

        // 4. Retornamos la data que Angular necesita para habilitar los menús (Cajero, Admin, etc.)
        return new AuthResponse(
                token,
                usuario.getUsername(),
                usuario.getIdUsuario(),
                usuario.getNombreCompleto(),
                usuario.getRol().name()
        );
    }
}