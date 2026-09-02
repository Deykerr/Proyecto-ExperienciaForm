package com.vircarmen.botica.controller;

import com.vircarmen.botica.dto.VentaRequest;
import com.vircarmen.botica.entity.Venta;
import com.vircarmen.botica.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PostMapping
    public ResponseEntity<Venta> registrarVenta(@RequestBody VentaRequest request) {
        Venta nuevaVenta = ventaService.registrarVenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVenta);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping
    public ResponseEntity<java.util.List<Venta>> listarVentas() {
        return ResponseEntity.ok(ventaService.obtenerTodasLasVentas());
    }
}
