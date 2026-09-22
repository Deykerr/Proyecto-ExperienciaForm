package com.vircarmen.botica.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.vircarmen.botica.dto.CompraRequest;
import com.vircarmen.botica.dto.CompraDTO;
import com.vircarmen.botica.dto.RecepcionCompraDTO;
import com.vircarmen.botica.dto.RecepcionCompraRequest;
import com.vircarmen.botica.dto.DevolucionProveedorDTO;
import com.vircarmen.botica.dto.DevolucionProveedorRequest;
import com.vircarmen.botica.service.CompraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping
    public ResponseEntity<CompraDTO> registrarCompra(@Valid @RequestBody CompraRequest request) {
        CompraDTO nuevaCompra = compraService.registrarCompra(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCompra);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @GetMapping
    public ResponseEntity<java.util.List<CompraDTO>> listar() {
        return ResponseEntity.ok(compraService.listar());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @GetMapping("/{id}")
    public ResponseEntity<CompraDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(compraService.obtener(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping("/{id}/recepciones")
    public ResponseEntity<RecepcionCompraDTO> recibir(@PathVariable Integer id,
            @Valid @RequestBody RecepcionCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.registrarRecepcion(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping("/{id}/devoluciones-proveedor")
    public ResponseEntity<DevolucionProveedorDTO> devolver(@PathVariable Integer id,
            @Valid @RequestBody DevolucionProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.devolverProveedor(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @GetMapping("/{id}/recepciones")
    public ResponseEntity<java.util.List<RecepcionCompraDTO>> listarRecepciones(@PathVariable Integer id) {
        return ResponseEntity.ok(compraService.listarRecepciones(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @GetMapping("/{id}/devoluciones-proveedor")
    public ResponseEntity<java.util.List<DevolucionProveedorDTO>> listarDevoluciones(@PathVariable Integer id) {
        return ResponseEntity.ok(compraService.listarDevoluciones(id));
    }
}
