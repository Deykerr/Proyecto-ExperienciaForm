package com.vircarmen.botica.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.vircarmen.botica.dto.ResumenDiarioSunatDTO;
import com.vircarmen.botica.service.ResumenDiarioSunatService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sunat/resumenes-diarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ResumenDiarioSunatController {
    private final ResumenDiarioSunatService service;

    @GetMapping
    public ResponseEntity<List<ResumenDiarioSunatDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PostMapping
    public ResponseEntity<ResumenDiarioSunatDTO> crear() {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearYEnviar());
    }

    @PostMapping("/{id}/procesar")
    public ResponseEntity<ResumenDiarioSunatDTO> procesar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.procesar(id));
    }

    @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> xml(@PathVariable Integer id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("resumen-diario-" + id + ".xml").build().toString())
                .body(service.obtenerXml(id).getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping(value = "/{id}/cdr", produces = "application/zip")
    public ResponseEntity<byte[]> cdr(@PathVariable Integer id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("R-resumen-diario-" + id + ".zip").build().toString())
                .body(service.obtenerCdr(id));
    }
}
