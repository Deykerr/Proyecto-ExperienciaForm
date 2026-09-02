package com.vircarmen.botica.controller;

import com.vircarmen.botica.dto.ProductoDTO;
import com.vircarmen.botica.dto.ProductoRequest;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // Todos pueden ver el catálogo (para consultar precios o stock)
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'ALMACENERO')")
    @GetMapping
    public ResponseEntity<Page<ProductoDTO>> listarProductos(Pageable pageable) {
        return ResponseEntity.ok(productoService.listarCatalogo(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'ALMACENERO')")
    @GetMapping("/buscar")
    public ResponseEntity<Page<ProductoDTO>> buscarProductos(@RequestParam String termino, Pageable pageable) {
        return ResponseEntity.ok(productoService.buscarProductosPorTermino(termino, pageable));
    }

    // El cajero, almacenero y admin necesitan esto para el escáner
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO', 'ALMACENERO')")
    @GetMapping("/barras/{codigoBarras}")
    public ResponseEntity<ProductoDTO> buscarPorCodigoBarras(@PathVariable String codigoBarras) {
        return ResponseEntity.ok(productoService.buscarPorCodigoBarras(codigoBarras));
    }

    // SOLO el Administrador o el Almacenero pueden crear o editar productos
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping
    public ResponseEntity<ProductoDTO> registrarProducto(@RequestBody ProductoRequest request) {
        ProductoDTO nuevoProducto = productoService.registrarProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    // Dentro de ProductoController.java

    // Actualizar producto completo
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Integer id, 
            @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, request));
    }

    // Dar de baja o activar producto (Baja lógica)
    // Usamos PatchMapping porque solo modificamos 1 campo (el estado)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(
            @PathVariable Integer id, 
            @RequestParam EstadoGeneral estado) { // Recibe 'A' o 'I'
        
        productoService.cambiarEstadoProducto(id, estado);
        return ResponseEntity.ok("Estado del producto actualizado a: " + estado.name());
    }
}