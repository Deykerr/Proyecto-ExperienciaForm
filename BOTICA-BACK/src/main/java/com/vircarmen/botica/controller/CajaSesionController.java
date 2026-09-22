package com.vircarmen.botica.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vircarmen.botica.dto.CajaSesionCierreRequest;
import com.vircarmen.botica.dto.CajaSesionDTO;
import com.vircarmen.botica.dto.CajaSesionRequest;
import com.vircarmen.botica.dto.MovimientoCajaRequest;
import com.vircarmen.botica.dto.ArqueoAprobacionRequest;
import com.vircarmen.botica.service.CajaSesionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaSesionController {

    private final CajaSesionService cajaSesionService;

   // Usamos hasAnyRole (Spring automáticamente buscará "ROLE_ADMIN" y "ROLE_CAJERO")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping("/abrir")
    public ResponseEntity<CajaSesionDTO> abrirCaja(@Valid @RequestBody CajaSesionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaSesionService.abrirCaja(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping("/cerrar/{idCaja}")
    public ResponseEntity<CajaSesionDTO> cerrarCaja(@PathVariable Integer idCaja, @Valid @RequestBody CajaSesionCierreRequest request) {
        return ResponseEntity.ok(cajaSesionService.cerrarCaja(idCaja, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/actual")
    public ResponseEntity<CajaSesionDTO> obtenerCajaActual() {
        CajaSesionDTO caja = cajaSesionService.obtenerCajaActual(null);

    if (caja == null) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(caja);
}

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping("/{idCaja}/movimientos")
    public ResponseEntity<CajaSesionDTO> registrarMovimiento(@PathVariable Integer idCaja,
            @Valid @RequestBody MovimientoCajaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaSesionService.registrarMovimiento(idCaja, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{idCaja}/arqueo/aprobar")
    public ResponseEntity<CajaSesionDTO> aprobarDiferencia(@PathVariable Integer idCaja,
            @Valid @RequestBody ArqueoAprobacionRequest request) {
        return ResponseEntity.ok(cajaSesionService.aprobarDiferencia(idCaja, request));
    }
}
