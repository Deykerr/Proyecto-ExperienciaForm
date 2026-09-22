package com.vircarmen.botica.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vircarmen.botica.dto.AuthRequest;
import com.vircarmen.botica.dto.AuthResponse;
import com.vircarmen.botica.service.AuthService;
import com.vircarmen.botica.service.AuthService.LoginResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    public static final String COOKIE_NAME = "BOTICA_SESSION";

    private final AuthService authService;

    @Value("${app.security.cookie-secure:true}")
    private boolean cookieSecure;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest) {
        LoginResult result = authService.login(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, crearCookie(result.token(), jwtExpiration).toString())
                .body(result.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, crearCookie("", 0).toString())
                .build();
    }

    private ResponseCookie crearCookie(String token, long duracionMilisegundos) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMillis(duracionMilisegundos))
                .build();
    }
}
