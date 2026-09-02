package com.vircarmen.botica.controller;

import com.vircarmen.botica.dto.ProveedorDTO;
import com.vircarmen.botica.dto.ProveedorRequest;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @GetMapping
    public ResponseEntity<Page<ProveedorDTO>> listarProveedores(Pageable pageable) {
        return ResponseEntity.ok(proveedorService.listarProveedores(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDTO> obtenerProveedor(@PathVariable Integer id) {
        return ResponseEntity.ok(proveedorService.obtenerProveedor(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping
    public ResponseEntity<ProveedorDTO> guardarProveedor(@Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.guardarProveedor(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTO> actualizarProveedor(@PathVariable Integer id, @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(proveedorService.actualizarProveedor(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProveedorDTO> cambiarEstado(@PathVariable Integer id, @RequestParam EstadoGeneral estado) {
        return ResponseEntity.ok(proveedorService.cambiarEstado(id, estado));
    }
}
