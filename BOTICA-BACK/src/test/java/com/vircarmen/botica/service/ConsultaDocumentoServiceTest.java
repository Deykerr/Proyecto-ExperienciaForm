package com.vircarmen.botica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class ConsultaDocumentoServiceTest {

    private HttpServer server;

    @AfterEach
    void detenerServidor() {
        if (server != null) server.stop(0);
    }

    @Test
    void deberiaConsultarDniConElContratoDeJsonPe() throws Exception {
        AtomicReference<String> metodo = new AtomicReference<>();
        AtomicReference<String> autorizacion = new AtomicReference<>();
        AtomicReference<String> cuerpo = new AtomicReference<>();
        iniciarServidor("/api/dni", exchange -> {
            metodo.set(exchange.getRequestMethod());
            autorizacion.set(exchange.getRequestHeaders().getFirst("Authorization"));
            cuerpo.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            responder(exchange, """
                    {"success":true,"data":{"numero":"12345678","nombres":"ANA MARIA",
                    "apellido_paterno":"TORRES","apellido_materno":"ROJAS"}}
                    """);
        });

        ConsultaDocumentoService.Resultado resultado = servicio().consultar("DNI", "12345678");

        assertTrue(resultado.encontrado());
        assertEquals("TORRES ROJAS ANA MARIA", resultado.nombreRazonSocial());
        assertEquals("POST", metodo.get());
        assertEquals("Bearer token-prueba", autorizacion.get());
        assertTrue(cuerpo.get().contains("\"dni\":\"12345678\""));
    }

    @Test
    void deberiaConsultarRucYMapearRazonSocialDireccionYEstado() throws Exception {
        AtomicReference<String> cuerpo = new AtomicReference<>();
        iniciarServidor("/api/ruc", exchange -> {
            cuerpo.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            responder(exchange, """
                    {"success":true,"data":{"ruc":"20123456789",
                    "nombre_o_razon_social":"BOTICA PRUEBA S.A.C.","estado":"ACTIVO",
                    "condicion":"HABIDO","direccion":"AV. PRINCIPAL 123"}}
                    """);
        });

        ConsultaDocumentoService.Resultado resultado = servicio().consultar("RUC", "20123456789");

        assertTrue(resultado.encontrado());
        assertEquals("BOTICA PRUEBA S.A.C.", resultado.nombreRazonSocial());
        assertEquals("AV. PRINCIPAL 123", resultado.direccion());
        assertEquals("Estado: ACTIVO · Condición: HABIDO", resultado.mensaje());
        assertTrue(cuerpo.get().contains("\"ruc\":\"20123456789\""));
    }

    private ConsultaDocumentoService servicio() {
        ConsultaDocumentoService servicio = new ConsultaDocumentoService(new ObjectMapper());
        ReflectionTestUtils.setField(servicio, "enabled", true);
        ReflectionTestUtils.setField(servicio, "provider", "JSON_PE");
        ReflectionTestUtils.setField(servicio, "baseUrl", "http://127.0.0.1:" + server.getAddress().getPort());
        ReflectionTestUtils.setField(servicio, "token", "token-prueba");
        ReflectionTestUtils.setField(servicio, "timeoutMs", 2000);
        return servicio;
    }

    private void iniciarServidor(String ruta, com.sun.net.httpserver.HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(ruta, handler);
        server.start();
    }

    private void responder(HttpExchange exchange, String json) throws IOException {
        byte[] respuesta = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, respuesta.length);
        try (var output = exchange.getResponseBody()) {
            output.write(respuesta);
        }
    }
}
