package com.vircarmen.botica.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vircarmen.botica.dto.AjusteInventarioRequest;
import com.vircarmen.botica.dto.ConciliacionStockDTO;
import com.vircarmen.botica.dto.ConciliarStockRequest;
import com.vircarmen.botica.dto.ConteoFisicoDTO;
import com.vircarmen.botica.dto.ConteoFisicoRequest;
import com.vircarmen.botica.dto.IngresoInventarioRequest;
import com.vircarmen.botica.dto.KardexDTO;
import com.vircarmen.botica.service.InventarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
public class InventarioController {
    private final InventarioService inventarioService;

    @PostMapping("/ingresos")
    public ResponseEntity<Void> registrarIngreso(@Valid @RequestBody IngresoInventarioRequest request) {
        inventarioService.registrarIngresoManual(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/ajustes")
    public ResponseEntity<Void> ajustarStock(@Valid @RequestBody AjusteInventarioRequest request) {
        inventarioService.ajustarStock(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conteos")
    public ResponseEntity<ConteoFisicoDTO> registrarConteo(@Valid @RequestBody ConteoFisicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.registrarConteo(request));
    }

    @GetMapping("/conteos")
    public ResponseEntity<List<ConteoFisicoDTO>> listarConteos() {
        return ResponseEntity.ok(inventarioService.listarConteos());
    }

    @GetMapping("/kardex")
    public ResponseEntity<Page<KardexDTO>> obtenerKardex(
            @RequestParam(required = false) Integer productoId,
            @RequestParam(required = false) Integer loteId,
            Pageable pageable) {
        return ResponseEntity.ok(inventarioService.obtenerKardex(productoId, loteId, pageable));
    }

    @GetMapping("/conciliacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConciliacionStockDTO>> obtenerDiferencias() {
        return ResponseEntity.ok(inventarioService.obtenerDiferenciasStock());
    }

    @PostMapping("/conciliacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConciliacionStockDTO>> conciliar(
            @Valid @RequestBody ConciliarStockRequest request) {
        return ResponseEntity.ok(inventarioService.conciliarStock(request));
    }
}
