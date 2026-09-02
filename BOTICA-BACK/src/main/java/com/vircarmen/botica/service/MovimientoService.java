package com.vircarmen.botica.service;

import com.vircarmen.botica.entity.DetalleMovimiento;
import com.vircarmen.botica.entity.Movimiento;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.TipoMovimiento;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.repository.DetalleMovimientoRepository;
import com.vircarmen.botica.repository.MovimientoRepository;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final com.vircarmen.botica.repository.LoteRepository loteRepository;

    @Transactional
    public void registrarIngresoAlmacen(Integer idProducto, Integer cantidad, String motivo, Integer idUsuario) {
        if (cantidad == null || cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        Producto producto = productoRepository.findById(Integer.valueOf(idProducto))
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Usuario usuario = usuarioRepository.findById(Integer.valueOf(idUsuario))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Movimiento movimiento = new Movimiento();
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setTipoMovimiento(TipoMovimiento.INGRESO);
        movimiento.setMotivo(motivo);
        movimiento.setUsuario(usuario);
        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);

        DetalleMovimiento detalle = new DetalleMovimiento();
        detalle.setMovimiento(movimientoGuardado);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(java.math.BigDecimal.ZERO != null ? java.math.BigDecimal.ZERO : BigDecimal.ZERO);
        detalleMovimientoRepository.save(detalle);

        producto.setStockActual(producto.getStockActual() + cantidad);
        productoRepository.save(producto);

        // CREAR LOTE AUTOMÁTICO PARA EVITAR CRASH EN VENTAS (FEFO)
        com.vircarmen.botica.entity.Lote lote = new com.vircarmen.botica.entity.Lote();
        lote.setCodigoLote("LOTE-AUT-" + System.currentTimeMillis());
        lote.setFechaVencimiento(java.time.LocalDate.now().plusYears(2));
        lote.setStockActual(cantidad);
        lote.setProducto(producto);
        loteRepository.save(lote);
    }
}
