package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.DetalleVentaDTO;
import com.vircarmen.botica.dto.VentaRequest;
import com.vircarmen.botica.entity.*;
import com.vircarmen.botica.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CajaSesionRepository cajaRepository;
    
    private final InventarioService inventarioService;
    private final PagoService pagoService;
    private final ComprobanteService comprobanteService;

    @Transactional(readOnly = true)
    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepository.findAll();
    }

    @Transactional
    public Venta registrarVenta(VentaRequest request) {
        Cliente cliente = null;
        if (request.getIdCliente() != null) {
            cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Cliente no encontrado"));
        }
        
        Integer idUsuario = com.vircarmen.botica.security.SecurityUtils.getUsuarioAutenticadoId();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Usuario no encontrado"));
                
        CajaSesion caja = cajaRepository.findByUsuarioIdUsuarioAndEstado(idUsuario, CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new com.vircarmen.botica.exception.CajaNoAbiertaException("El usuario no tiene una caja abierta."));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setCajaSesion(caja);
        venta.setFechaEmision(LocalDateTime.now());
        venta.setEstado(EstadoVenta.PAGADA);
        
        BigDecimal subtotalGeneral = BigDecimal.ZERO;

        for (DetalleVentaDTO reqItem : request.getItems()) {
            Producto producto = productoRepository.findByIdWithLock(reqItem.idProducto())
                    .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Producto no encontrado"));
                    
            List<DetalleVenta> detallesFEFO = inventarioService.consumirStockFEFO(producto, reqItem.cantidad(), venta);
            
            for (DetalleVenta det : detallesFEFO) {
                subtotalGeneral = subtotalGeneral.add(det.getSubtotal());
                venta.getDetalles().add(det);
            }
        }
        
        // Calcular impuestos (suponiendo IGV 18%)
        BigDecimal tasaIgv = new BigDecimal("0.18");
        BigDecimal total = subtotalGeneral;
        BigDecimal subtotal = total.divide(BigDecimal.ONE.add(tasaIgv), 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal);
        
        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);

        // Guardar venta inicial para tener ID
        venta = ventaRepository.save(venta);
        
        // Pagos
        pagoService.procesarPagos(venta, request.getPagos(), caja);
        
        // Comprobante
        Comprobante comprobante = comprobanteService.generarComprobante(venta, request.getTipoComprobante());
        venta.setComprobante(comprobante);

        return ventaRepository.save(venta);
    }
}
