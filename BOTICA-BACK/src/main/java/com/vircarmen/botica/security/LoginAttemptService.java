package com.vircarmen.botica.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.vircarmen.botica.exception.TooManyRequestsException;

@Service
public class LoginAttemptService {
    private static final int MAX_INTENTOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Intentos> intentos = new ConcurrentHashMap<>();

    public void verificarPermitido(String username, String ip) {
        String clave = clave(username, ip);
        Intentos estado = intentos.get(clave);
        if (estado == null) {
            return;
        }
        if (estado.bloqueadoHasta() != null && estado.bloqueadoHasta().isAfter(Instant.now())) {
            throw new TooManyRequestsException(
                    "Demasiados intentos fallidos. Intenta nuevamente en unos minutos.");
        }
        if (estado.inicio().plus(VENTANA).isBefore(Instant.now())) {
            intentos.remove(clave);
        }
    }

    public void registrarFallo(String username, String ip) {
        String clave = clave(username, ip);
        Instant ahora = Instant.now();
        intentos.compute(clave, (ignored, actual) -> {
            if (actual == null || actual.inicio().plus(VENTANA).isBefore(ahora)) {
                return new Intentos(1, ahora, null);
            }
            int cantidad = actual.cantidad() + 1;
            Instant bloqueadoHasta = cantidad >= MAX_INTENTOS ? ahora.plus(VENTANA) : null;
            return new Intentos(cantidad, actual.inicio(), bloqueadoHasta);
        });
    }

    public void registrarExito(String username, String ip) {
        intentos.remove(clave(username, ip));
    }

    private String clave(String username, String ip) {
        String usuarioNormalizado = username == null
                ? ""
                : username.trim().toLowerCase(Locale.ROOT);
        return usuarioNormalizado + "|" + (ip == null ? "desconocida" : ip);
    }

    private record Intentos(int cantidad, Instant inicio, Instant bloqueadoHasta) {}
}
