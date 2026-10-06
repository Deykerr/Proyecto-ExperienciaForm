package com.vircarmen.botica.controller;

import com.vircarmen.botica.dto.ClienteDTO;
import com.vircarmen.botica.dto.ClienteRequest;
import com.vircarmen.botica.dto.ConsultaDocumentoDTO;
import com.vircarmen.botica.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
// Aplicamos restricción a TODA la clase: Solo Caja y Admin manejan clientes
@PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')") 
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @GetMapping("/resolver")
    public ResponseEntity<ConsultaDocumentoDTO> resolverDocumento(@RequestParam String numeroDocumento) {
        return ResponseEntity.ok(clienteService.resolverDocumento(numeroDocumento));
    }

    @GetMapping("/{numeroDocumento}")
    public ResponseEntity<ClienteDTO> buscarPorDocumento(@PathVariable String numeroDocumento) {
        return ResponseEntity.ok(clienteService.buscarPorDocumento(numeroDocumento));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteDTO>> buscarPorCriterio(
            @RequestParam String criterio, 
            @RequestParam String termino) {
        return ResponseEntity.ok(clienteService.buscarPorCriterio(criterio, termino));
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> registrarCliente(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.registrarCliente(request));
    }
    
    // Cajeros y Admins pueden corregir datos de clientes
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizarCliente(
            @PathVariable Integer id, 
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }
}
