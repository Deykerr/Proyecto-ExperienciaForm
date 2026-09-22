package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.LoteDTO;
import com.vircarmen.botica.entity.EstadoLote;
import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.repository.LoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;
    @InjectMocks
    private LoteService loteService;

    @Test
    void deberiaMapearLoteSinExponerEntidadesJpa() {
        Producto producto = new Producto();
        producto.setIdProducto(7);
        producto.setNombre("Producto de prueba");
        producto.setCodigoBarras("775000000007");

        Lote lote = new Lote();
        lote.setIdLote(12);
        lote.setCodigoLote("L-012");
        lote.setFechaIngreso(LocalDate.now().minusDays(10));
        lote.setFechaVencimiento(LocalDate.now().plusMonths(6));
        lote.setStockInicial(20);
        lote.setStockActual(15);
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote.setProducto(producto);
        when(loteRepository.findAllWithProducto()).thenReturn(List.of(lote));

        List<LoteDTO> resultado = loteService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Producto de prueba", resultado.getFirst().producto());
        assertEquals(7, resultado.getFirst().idProducto());
        assertEquals("L-012", resultado.getFirst().codigoLote());
    }
}
