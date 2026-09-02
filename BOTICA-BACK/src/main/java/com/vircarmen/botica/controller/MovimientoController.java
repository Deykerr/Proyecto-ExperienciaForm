package com.vircarmen.botica.controller;

import com.vircarmen.botica.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    // ¡Peligro! Solo Almacén y Admin pueden inyectar stock. El cajero lo tiene prohibido.
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping("/ingreso")
    public ResponseEntity<String> registrarIngreso(
            @RequestParam Integer idProducto,
            @RequestParam Integer cantidad,
            @RequestParam String motivo,
            @RequestParam Integer idUsuario) {
        
        movimientoService.registrarIngresoAlmacen(idProducto, cantidad, motivo, idUsuario);
        return ResponseEntity.ok("Stock ingresado correctamente");
    }
}