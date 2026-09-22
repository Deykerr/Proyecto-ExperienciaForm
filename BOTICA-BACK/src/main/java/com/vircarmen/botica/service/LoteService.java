package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.LoteDTO;
import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public List<LoteDTO> listarTodos() {
        return loteRepository.findAllWithProducto().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LoteDTO> listarConStockPorVencimiento() {
        return loteRepository.findLotesProximosAVencer().stream().map(this::toDto).toList();
    }

    private LoteDTO toDto(Lote lote) {
        return new LoteDTO(
                lote.getIdLote(),
                lote.getCodigoLote(),
                lote.getFechaIngreso(),
                lote.getFechaVencimiento(),
                lote.getStockInicial(),
                lote.getStockActual(),
                lote.getPrecioCompra(),
                lote.getEstado().name(),
                lote.getProducto().getIdProducto(),
                lote.getProducto().getNombre(),
                lote.getProducto().getCodigoBarras());
    }
}
