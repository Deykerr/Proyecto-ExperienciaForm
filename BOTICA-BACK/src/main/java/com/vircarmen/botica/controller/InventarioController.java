package com.vircarmen.botica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vircarmen.botica.dto.AjusteInventarioRequest;
import com.vircarmen.botica.service.InventarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping("/ajuste")
    public ResponseEntity<String> ajustarStock(@RequestBody AjusteInventarioRequest request) {
        // En un paso futuro se implementar la lgica completa de ajuste aqu
        return ResponseEntity.ok("Ajuste registrado correctamente (Simulado)");
    }
}
