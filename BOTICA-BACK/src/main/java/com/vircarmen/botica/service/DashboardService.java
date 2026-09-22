package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.*;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.entity.Venta;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.LoteRepository;
import com.vircarmen.botica.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public MetricasDTO obtenerMetricasHoy() {
        // Obtenemos todas las ventas para que se refleje el historial completo en el Dashboard
        List<Venta> ventasHoy = ventaRepository.findAll();
        
        BigDecimal totalVentas = ventasHoy.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        int cantidadOperaciones = ventasHoy.size();
        
        BigDecimal ticketPromedio = BigDecimal.ZERO;
        if (cantidadOperaciones > 0) {
            ticketPromedio = totalVentas.divide(new BigDecimal(cantidadOperaciones), 2, RoundingMode.HALF_UP);
        }
        
        return new MetricasDTO(totalVentas, cantidadOperaciones, ticketPromedio);
    }

    @Transactional(readOnly = true)
    public List<BajoStockDTO> obtenerBajoStock() {
        // Encontrar productos cuyo stockActual <= stockMinimo
        List<Producto> productos = productoRepository.findProductosConStockBajo();
                
        return productos.stream()
                .map(p -> new BajoStockDTO(p.getCodigoBarras(), p.getNombre(), p.getStockActual(), p.getStockMinimo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoteVencerDTO> obtenerLotesPorVencer() {
        // Encontrar lotes que venzan en los proximos 90 dias
        LocalDate limite = LocalDate.now().plusDays(90);
        
        List<Lote> lotes = loteRepository.findLotesConStockHastaFecha(limite).stream()
                .filter(l -> !l.getFechaVencimiento().isBefore(LocalDate.now()))
                .toList();
                
        return lotes.stream()
                .map(l -> new LoteVencerDTO(l.getCodigoLote(), l.getProducto().getNombre(), l.getFechaVencimiento().toString(), l.getStockActual()))
                .toList();
    }
}
