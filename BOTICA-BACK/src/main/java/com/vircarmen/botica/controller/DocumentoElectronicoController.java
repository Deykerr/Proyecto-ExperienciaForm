package com.vircarmen.botica.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.vircarmen.botica.dto.DocumentoElectronicoDTO;
import com.vircarmen.botica.service.DocumentoElectronicoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sunat/documentos")
@RequiredArgsConstructor
public class DocumentoElectronicoController {
    private final DocumentoElectronicoService service;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<DocumentoElectronicoDTO>> listar() { return ResponseEntity.ok(service.listar()); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/procesar")
    public ResponseEntity<DocumentoElectronicoDTO> procesar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.procesar(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> xml(@PathVariable Integer id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("documento-" + id + ".xml").build().toString())
                .body(service.obtenerXml(id).getBytes(StandardCharsets.UTF_8));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{id}/cdr", produces = "application/zip")
    public ResponseEntity<byte[]> cdr(@PathVariable Integer id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("R-documento-" + id + ".zip").build().toString())
                .body(service.obtenerCdr(id));
    }
}
