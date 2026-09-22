package com.vircarmen.botica.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.vircarmen.botica.dto.RecetaDTO;
import com.vircarmen.botica.dto.RecetaRequest;
import com.vircarmen.botica.service.RecetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recetas")
@RequiredArgsConstructor
public class RecetaController {
    private final RecetaService recetaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping
    public ResponseEntity<RecetaDTO> registrar(@Valid @RequestBody RecetaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recetaService.registrar(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping
    public ResponseEntity<List<RecetaDTO>> listar() { return ResponseEntity.ok(recetaService.listar()); }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/{id}")
    public ResponseEntity<RecetaDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(recetaService.obtener(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/anular")
    public ResponseEntity<RecetaDTO> anular(@PathVariable Integer id, @RequestParam String motivo) {
        return ResponseEntity.ok(recetaService.anular(id, motivo));
    }
}
