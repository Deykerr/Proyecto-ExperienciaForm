package com.vircarmen.botica.controller;

import com.vircarmen.botica.dto.VentaRequest;
import com.vircarmen.botica.dto.VentaResumenDTO;
import com.vircarmen.botica.service.VentaService;
import com.vircarmen.botica.service.DevolucionVentaService;
import com.vircarmen.botica.dto.DevolucionVentaRequest;
import com.vircarmen.botica.dto.DevolucionVentaDTO;
import com.vircarmen.botica.dto.AnulacionVentaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final DevolucionVentaService devolucionVentaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping
    public ResponseEntity<VentaResumenDTO> registrarVenta(@Valid @RequestBody VentaRequest request) {
        VentaResumenDTO nuevaVenta = ventaService.registrarVenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVenta);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping
    public ResponseEntity<java.util.List<VentaResumenDTO>> listarVentas() {
        return ResponseEntity.ok(ventaService.obtenerTodasLasVentas());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping("/{id}/devoluciones")
    public ResponseEntity<DevolucionVentaDTO> devolver(@PathVariable Integer id,
            @Valid @RequestBody DevolucionVentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucionVentaService.devolver(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/anular")
    public ResponseEntity<DevolucionVentaDTO> anular(@PathVariable Integer id,
            @Valid @RequestBody AnulacionVentaRequest request) {
        return ResponseEntity.ok(devolucionVentaService.anular(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/{id}/devoluciones")
    public ResponseEntity<java.util.List<DevolucionVentaDTO>> listarDevoluciones(@PathVariable Integer id) {
        return ResponseEntity.ok(devolucionVentaService.listarPorVenta(id));
    }
}
