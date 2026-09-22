package com.vircarmen.botica.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.vircarmen.botica.config.SunatProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SunatSoapClient {
    private final SunatProperties properties;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20)).build();

    public Respuesta enviar(String nombreXml, String xmlFirmado) throws Exception {
        byte[] zip = comprimir(nombreXml, xmlFirmado);
        String cuerpo = """
                    <ser:sendBill>
                      <fileName>%s.zip</fileName>
                      <contentFile>%s</contentFile>
                    </ser:sendBill>
                """.formatted(esc(nombreXml), Base64.getEncoder().encodeToString(zip));
        Document soap = enviarSoap(cuerpo, "urn:sendBill");
        var applications = soap.getElementsByTagNameNS("*", "applicationResponse");
        if (applications.getLength() == 0) throw new IllegalStateException("SUNAT no devolvió una CDR.");
        return interpretarCdr(Base64.getDecoder().decode(applications.item(0).getTextContent().trim()));
    }

    public String enviarResumen(String nombreXml, String xmlFirmado) throws Exception {
        byte[] zip = comprimir(nombreXml, xmlFirmado);
        String cuerpo = """
                    <ser:sendSummary>
                      <fileName>%s.zip</fileName>
                      <contentFile>%s</contentFile>
                    </ser:sendSummary>
                """.formatted(esc(nombreXml), Base64.getEncoder().encodeToString(zip));
        Document soap = enviarSoap(cuerpo, "urn:sendSummary");
        var tickets = soap.getElementsByTagNameNS("*", "ticket");
        if (tickets.getLength() == 0 || tickets.item(0).getTextContent().isBlank()) {
            throw new IllegalStateException("SUNAT no devolvió el ticket del Resumen Diario.");
        }
        return tickets.item(0).getTextContent().trim();
    }

    public EstadoTicket consultarResumen(String ticket) throws Exception {
        String cuerpo = """
                    <ser:getStatus>
                      <ticket>%s</ticket>
                    </ser:getStatus>
                """.formatted(esc(ticket));
        Document soap = enviarSoap(cuerpo, "urn:getStatus");
        String estado = texto(soap, "statusCode");
        var contents = soap.getElementsByTagNameNS("*", "content");
        if (contents.getLength() > 0 && !contents.item(0).getTextContent().isBlank()) {
            Respuesta cdr = interpretarCdr(Base64.getDecoder().decode(contents.item(0).getTextContent().trim()));
            return new EstadoTicket(true, estado, cdr.cdr(), cdr.codigo(), cdr.descripcion(),
                    cdr.aceptada(), cdr.observada());
        }
        if ("98".equals(estado)) {
            return new EstadoTicket(false, estado, null, null, "Resumen en proceso por SUNAT.", false, false);
        }
        if ("99".equals(estado)) {
            return new EstadoTicket(true, estado, null, estado,
                    "SUNAT rechazó el procesamiento del Resumen Diario.", false, false);
        }
        throw new IllegalStateException("SUNAT devolvió un estado de ticket no reconocido: " + estado);
    }

    private Document enviarSoap(String cuerpo, String soapAction) throws Exception {
        String usuario = properties.getRuc() + properties.getSolUser();
        String envelope = """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:ser="http://service.sunat.gob.pe"
                                  xmlns:wsse="http://schemas.xmlsoap.org/ws/2002/12/secext">
                  <soapenv:Header>
                    <wsse:Security>
                      <wsse:UsernameToken>
                        <wsse:Username>%s</wsse:Username>
                        <wsse:Password>%s</wsse:Password>
                      </wsse:UsernameToken>
                    </wsse:Security>
                  </soapenv:Header>
                  <soapenv:Body>
                    %s
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(esc(usuario), esc(properties.getSolPassword()), cuerpo);
        HttpRequest request = HttpRequest.newBuilder(URI.create(properties.endpoint()))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", soapAction)
                .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8)).build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("SUNAT respondió HTTP " + response.statusCode());
        }
        Document soap = parse(response.body());
        var faults = soap.getElementsByTagNameNS("*", "faultstring");
        if (faults.getLength() > 0) throw new IllegalStateException("SUNAT: " + faults.item(0).getTextContent());
        return soap;
    }

    private Respuesta interpretarCdr(byte[] cdrZip) throws Exception {
        byte[] cdrXml = extraerPrimero(cdrZip);
        Document cdr = parse(cdrXml);
        String codigo = texto(cdr, "ResponseCode");
        String descripcion = texto(cdr, "Description");
        boolean observada = cdr.getElementsByTagNameNS("*", "Note").getLength() > 0;
        return new Respuesta(cdrZip, codigo, descripcion, "0".equals(codigo), observada);
    }

    private byte[] comprimir(String nombre, String xml) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            zip.putNextEntry(new ZipEntry(nombre + ".xml"));
            zip.write(xml.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return out.toByteArray();
    }

    private byte[] extraerPrimero(byte[] zipBytes) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            if (zip.getNextEntry() == null) throw new IllegalStateException("La CDR está vacía.");
            return zip.readAllBytes();
        }
    }

    private Document parse(byte[] bytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }

    private String texto(Document document, String localName) {
        var nodes = document.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent();
    }

    private String esc(String valor) {
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    public record Respuesta(byte[] cdr, String codigo, String descripcion, boolean aceptada, boolean observada) {}
    public record EstadoTicket(boolean terminado, String estadoTicket, byte[] cdr, String codigo,
                                String descripcion, boolean aceptada, boolean observada) {}
}
