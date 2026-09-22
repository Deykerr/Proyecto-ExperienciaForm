package com.vircarmen.botica.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vircarmen.botica.dto.IngresoInventarioRequest;
import com.vircarmen.botica.service.MovimientoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {
    private final MovimientoService movimientoService;

    /**
     * Ruta conservada por compatibilidad. Los clientes nuevos deben usar
     * POST /api/inventario/ingresos.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping("/ingreso")
    public ResponseEntity<Void> registrarIngreso(@Valid @RequestBody IngresoInventarioRequest request) {
        movimientoService.registrarIngresoAlmacen(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
