package com.vircarmen.botica.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ConsultaDocumentoService {
    private static final Logger LOG = LoggerFactory.getLogger(ConsultaDocumentoService.class);

    private final ObjectMapper objectMapper;

    @Value("${app.consulta-documento.enabled:false}")
    private boolean enabled;

    @Value("${app.consulta-documento.provider:JSON_PE}")
    private String provider;

    @Value("${app.consulta-documento.base-url:https://api.json.pe}")
    private String baseUrl;

    @Value("${app.consulta-documento.token:}")
    private String token;

    @Value("${app.consulta-documento.timeout-ms:3000}")
    private int timeoutMs;

    public ConsultaDocumentoService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Resultado consultar(String tipoDocumento, String numeroDocumento) {
        if (!enabled) {
            return Resultado.noDisponible("La consulta automática no está configurada.");
        }
        if (!"JSON_PE".equalsIgnoreCase(provider)) {
            return Resultado.noDisponible("El proveedor de identidad configurado no es compatible.");
        }
        if (token == null || token.isBlank()) {
            return Resultado.noDisponible("Falta configurar el token de JSON.pe.");
        }

        try {
            boolean esDni = "DNI".equalsIgnoreCase(tipoDocumento);
            String endpoint = esDni ? "/api/dni" : "/api/ruc";
            String campo = esDni ? "dni" : "ruc";
            String cuerpo = objectMapper.writeValueAsString(Map.of(campo, numeroDocumento));
            String url = baseUrl.replaceAll("/+$", "") + endpoint;
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(Math.max(timeoutMs, 500)))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token.trim())
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(Math.max(timeoutMs, 500)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                return Resultado.noEncontrado();
            }
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                return Resultado.noDisponible("El token de JSON.pe es inválido o no tiene permisos.");
            }
            if (response.statusCode() == 402) {
                return Resultado.noDisponible("La cuenta de JSON.pe no tiene créditos disponibles.");
            }
            if (response.statusCode() == 429) {
                return Resultado.noDisponible("JSON.pe limitó temporalmente las consultas. Registra el cliente manualmente.");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOG.warn("JSON.pe respondió con HTTP {}", response.statusCode());
                return Resultado.noDisponible("JSON.pe no está disponible. Registra el cliente manualmente.");
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (root.has("success") && !root.path("success").asBoolean()) {
                String mensaje = primerTexto(root, "message", "mensaje", "error");
                return Resultado.noDisponible(mensaje == null ? "JSON.pe no encontró el documento." : mensaje);
            }
            JsonNode data = seleccionarPayload(root);
            String nombre = nombreCompleto(data);
            if (nombre == null || nombre.isBlank()) {
                return Resultado.noEncontrado();
            }
            String detalle = detalleResultado(tipoDocumento, data);
            return new Resultado(true, nombre.trim(), primerTexto(data, "direccion", "address"), detalle);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Resultado.noDisponible("La consulta de identidad fue interrumpida.");
        } catch (Exception ex) {
            LOG.warn("No se pudo consultar JSON.pe: {}", ex.getMessage());
            return Resultado.noDisponible("No se pudo consultar JSON.pe. Registra el cliente manualmente.");
        }
    }

    private JsonNode seleccionarPayload(JsonNode root) {
        for (String campo : List.of("data", "result", "resultado")) {
            JsonNode candidato = root.path(campo);
            if (candidato.isObject()) return candidato;
        }
        return root;
    }

    private String nombreCompleto(JsonNode data) {
        String directo = primerTexto(data, "nombreCompleto", "nombre_completo", "fullName", "full_name",
                "nombreRazonSocial", "razonSocial", "razon_social", "nombre_o_razon_social");
        if (directo != null) return directo;
        String nombres = primerTexto(data, "nombres", "names");
        String paterno = primerTexto(data, "apellidoPaterno", "apellido_paterno", "firstSurname");
        String materno = primerTexto(data, "apellidoMaterno", "apellido_materno", "secondSurname");
        String combinado = String.join(" ", valor(paterno), valor(materno), valor(nombres)).trim();
        return combinado.isBlank() ? null : combinado.replaceAll("\\s+", " ");
    }

    private String primerTexto(JsonNode data, String... campos) {
        for (String campo : campos) {
            JsonNode valor = data.path(campo);
            if (valor.isTextual() && !valor.asText().isBlank()) return valor.asText();
        }
        return null;
    }

    private String valor(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String detalleResultado(String tipoDocumento, JsonNode data) {
        if (!"RUC".equalsIgnoreCase(tipoDocumento)) {
            return "Datos obtenidos de JSON.pe. Confirma el nombre antes de guardar.";
        }
        String estado = primerTexto(data, "estado");
        String condicion = primerTexto(data, "condicion");
        String detalle = String.join(" · ",
                estado == null ? "" : "Estado: " + estado.toUpperCase(Locale.ROOT),
                condicion == null ? "" : "Condición: " + condicion.toUpperCase(Locale.ROOT));
        detalle = detalle.replaceAll("^( · )|( · )$", "");
        return detalle.isBlank() ? "RUC obtenido de JSON.pe. Confirma los datos antes de guardar." : detalle;
    }

    public record Resultado(boolean encontrado, String nombreRazonSocial, String direccion, String mensaje) {
        static Resultado noEncontrado() {
            return new Resultado(false, null, null, "El documento no fue encontrado por el proveedor.");
        }

        static Resultado noDisponible(String mensaje) {
            return new Resultado(false, null, null, mensaje);
        }
    }
}
