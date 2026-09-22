package com.vircarmen.botica.service;

import org.springframework.stereotype.Service;

import com.vircarmen.botica.dto.IngresoInventarioRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimientoService {
    private final InventarioService inventarioService;

    public void registrarIngresoAlmacen(IngresoInventarioRequest request) {
        inventarioService.registrarIngresoManual(request);
    }
}
