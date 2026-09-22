package com.vircarmen.botica.service;

import java.util.HashMap;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.vircarmen.botica.dto.AuthRequest;
import com.vircarmen.botica.dto.AuthResponse;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.JwtService;
import com.vircarmen.botica.security.LoginAttemptService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;

    public LoginResult login(AuthRequest request, String clientIp) {
        loginAttemptService.verificarPermitido(request.username(), clientIp);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException ex) {
            loginAttemptService.registrarFallo(request.username(), clientIp);
            throw new BusinessException("Usuario o contraseña incorrectos.");
        }
        loginAttemptService.registrarExito(request.username(), clientIp);

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(new HashMap<>(), userDetails);
        AuthResponse response = new AuthResponse(
                usuario.getUsername(),
                usuario.getIdUsuario(),
                usuario.getNombreCompleto(),
                usuario.getRol().name());
        return new LoginResult(token, response);
    }

    public record LoginResult(String token, AuthResponse response) {}
}
