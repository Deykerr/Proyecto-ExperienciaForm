package com.vircarmen.botica.controller;

import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.repository.LoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    @Autowired
    private LoteRepository loteRepository;

    @GetMapping
    public List<Lote> listarLotes() {
        return loteRepository.findAll();
    }
    
    @GetMapping("/vencimiento")
    public List<Lote> listarLotesProximosVencer() {
        return loteRepository.findLotesProximosAVencer();
    }
}
