package com.vircarmen.botica.controller;

import com.vircarmen.botica.dto.LoteDTO;
import com.vircarmen.botica.service.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    @GetMapping
    public ResponseEntity<List<LoteDTO>> listarLotes() {
        return ResponseEntity.ok(loteService.listarTodos());
    }
    
    @GetMapping("/vencimiento")
    public ResponseEntity<List<LoteDTO>> listarLotesProximosVencer() {
        return ResponseEntity.ok(loteService.listarConStockPorVencimiento());
    }
}
