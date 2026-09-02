package com.vircarmen.botica.controller;

import com.vircarmen.botica.dto.LaboratorioDTO;
import com.vircarmen.botica.dto.LaboratorioRequest;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.service.LaboratorioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/laboratorios")
@RequiredArgsConstructor
public class LaboratorioController {

    private final LaboratorioService laboratorioService;

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'CAJERO')")
    @GetMapping
    public ResponseEntity<Page<LaboratorioDTO>> listarLaboratorios(Pageable pageable) {
        return ResponseEntity.ok(laboratorioService.listarLaboratorios(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @GetMapping("/{id}")
    public ResponseEntity<LaboratorioDTO> obtenerLaboratorio(@PathVariable Integer id) {
        return ResponseEntity.ok(laboratorioService.obtenerLaboratorio(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping
    public ResponseEntity<LaboratorioDTO> guardarLaboratorio(@Valid @RequestBody LaboratorioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratorioService.guardarLaboratorio(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PutMapping("/{id}")
    public ResponseEntity<LaboratorioDTO> actualizarLaboratorio(@PathVariable Integer id, @Valid @RequestBody LaboratorioRequest request) {
        return ResponseEntity.ok(laboratorioService.actualizarLaboratorio(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<LaboratorioDTO> cambiarEstado(@PathVariable Integer id, @RequestParam EstadoGeneral estado) {
        return ResponseEntity.ok(laboratorioService.cambiarEstado(id, estado));
    }
}
