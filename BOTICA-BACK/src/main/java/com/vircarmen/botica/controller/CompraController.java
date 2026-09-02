package com.vircarmen.botica.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vircarmen.botica.dto.CompraRequest;
import com.vircarmen.botica.entity.Compra;
import com.vircarmen.botica.service.CompraService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    @PostMapping
    public ResponseEntity<Compra> registrarCompra(@RequestBody CompraRequest request) {
        Compra nuevaCompra = compraService.registrarCompra(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCompra);
    }
}
