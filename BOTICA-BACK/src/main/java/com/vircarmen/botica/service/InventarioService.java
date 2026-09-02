    package com.vircarmen.botica.service;

    import java.time.LocalDate;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.List;
    import java.util.stream.Collectors;

    import org.springframework.stereotype.Service;

    import com.vircarmen.botica.entity.DetalleVenta;
    import com.vircarmen.botica.entity.EstadoLote;
    import com.vircarmen.botica.entity.Lote;
    import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.Venta;
import com.vircarmen.botica.repository.LoteRepository;
import com.vircarmen.botica.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

    @Service
    @RequiredArgsConstructor
    public class InventarioService {
        private final LoteRepository loteRepository;
        private final ProductoRepository productoRepository;

        public List<DetalleVenta> consumirStockFEFO(Producto producto, Integer cantidadRequerida, Venta venta) {
            // Filtrar lotes disponibles y no vencidos
            List<Lote> lotesDisponibles = producto.getLotes().stream()
                    .filter(l -> l.getEstado() == EstadoLote.DISPONIBLE)
                    .filter(l -> l.getStockActual() > 0)
                    .filter(l -> l.getFechaVencimiento().isAfter(LocalDate.now()))
                    .sorted(Comparator.comparing(Lote::getFechaVencimiento))
                    .collect(Collectors.toList());

            int totalDisponible = lotesDisponibles.stream().mapToInt(Lote::getStockActual).sum();
            if (totalDisponible < cantidadRequerida) {
                throw new com.vircarmen.botica.exception.StockInsuficienteException("Stock insuficiente para el producto: " + producto.getNombre() + ". Requerido: " + cantidadRequerida + ", Disponible: " + totalDisponible);
            }

            List<DetalleVenta> detallesGenerados = new ArrayList<>();
            int cantidadRestante = cantidadRequerida;

            for (Lote lote : lotesDisponibles) {
                if (cantidadRestante <= 0) break;

                int cantidadTomada = Math.min(lote.getStockActual(), cantidadRestante);
                lote.setStockActual(lote.getStockActual() - cantidadTomada);
                if (lote.getStockActual() == 0) {
                    lote.setEstado(EstadoLote.AGOTADO);
                }
                loteRepository.save(lote);

                DetalleVenta det = new DetalleVenta();
                det.setVenta(venta);
                det.setLote(lote);
                det.setCantidad(cantidadTomada);
                det.setPrecioUnitario(producto.getPrecioVenta());
                det.setSubtotal(producto.getPrecioVenta().multiply(new java.math.BigDecimal(cantidadTomada)));
                detallesGenerados.add(det);

                cantidadRestante -= cantidadTomada;
            }

            // Actualizar stock denormalizado en producto
            producto.setStockActual(producto.getStockActual() - cantidadRequerida);
            productoRepository.save(producto);

            return detallesGenerados;
        }
    }
