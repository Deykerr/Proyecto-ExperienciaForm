package com.vircarmen.botica.controller;

import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.vircarmen.botica.dto.ReporteDigemidDTO;
import com.vircarmen.botica.dto.ResultadoReporteDigemidRequest;
import com.vircarmen.botica.service.ReporteDigemidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/digemid/reportes-precios")
@RequiredArgsConstructor
public class ReporteDigemidController {
    private final ReporteDigemidService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ReporteDigemidDTO> generar(@RequestParam String periodo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.generar(periodo));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ReporteDigemidDTO>> listar() { return ResponseEntity.ok(service.listar()); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/resultado")
    public ResponseEntity<ReporteDigemidDTO> resultado(@PathVariable Integer id,
            @Valid @RequestBody ResultadoReporteDigemidRequest request) {
        return ResponseEntity.ok(service.registrarResultado(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{id}/archivo", produces = "text/csv")
    public ResponseEntity<byte[]> descargar(@PathVariable Integer id) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("reporte-precios-digemid-" + id + ".csv").build().toString())
                .body(service.descargar(id));
    }
}
