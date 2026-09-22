package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.ResumenAlertasInventarioDTO;
import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.LoteRepository;
import com.vircarmen.botica.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaInventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private LoteRepository loteRepository;
    @InjectMocks
    private AlertaInventarioService service;

    @Test
    void deberiaClasificarStockYVencimientosSinDuplicarCategorias() {
        Producto agotado = producto(1, "Paracetamol", 0, 5);
        Producto bajo = producto(2, "Ibuprofeno", 4, 5);
        Lote vencido = lote(10, "L-ANT", agotado, LocalDate.now().minusDays(2), 3);
        Lote proximo = lote(11, "L-PROX", bajo, LocalDate.now().plusDays(5), 6);

        when(productoRepository.findProductosConStockBajo()).thenReturn(List.of(agotado, bajo));
        when(loteRepository.findLotesConStockHastaFecha(any())).thenReturn(List.of(vencido, proximo));

        ResumenAlertasInventarioDTO resumen = service.obtenerResumen(30);

        assertEquals(4, resumen.total());
        assertEquals(3, resumen.criticas());
        assertEquals(1, resumen.advertencias());
        assertEquals(1, resumen.agotados());
        assertEquals(1, resumen.bajoStock());
        assertEquals(1, resumen.vencidosConStock());
        assertEquals(1, resumen.proximosAVencer());
        assertEquals("LOTE_VENCIDO", resumen.alertas().getFirst().tipo());
    }

    @Test
    void deberiaRechazarRangoDeVencimientoFueraDeLimite() {
        assertThrows(BusinessException.class, () -> service.obtenerResumen(0));
        assertThrows(BusinessException.class, () -> service.obtenerResumen(366));
    }

    private Producto producto(int id, String nombre, int stock, int minimo) {
        Producto producto = new Producto();
        producto.setIdProducto(id);
        producto.setNombre(nombre);
        producto.setCodigoBarras("COD-" + id);
        producto.setStockActual(stock);
        producto.setStockMinimo(minimo);
        return producto;
    }

    private Lote lote(int id, String codigo, Producto producto, LocalDate vencimiento, int stock) {
        Lote lote = new Lote();
        lote.setIdLote(id);
        lote.setCodigoLote(codigo);
        lote.setProducto(producto);
        lote.setFechaVencimiento(vencimiento);
        lote.setStockActual(stock);
        return lote;
    }
}
