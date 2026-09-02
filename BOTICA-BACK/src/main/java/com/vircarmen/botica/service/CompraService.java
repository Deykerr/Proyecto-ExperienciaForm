package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.CompraRequest;
import com.vircarmen.botica.dto.DetalleCompraRequest;
import com.vircarmen.botica.entity.*;
import com.vircarmen.botica.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    @Transactional
    public Compra registrarCompra(CompraRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
                .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Proveedor no encontrado"));
                
        Integer idUsuario = com.vircarmen.botica.security.SecurityUtils.getUsuarioAutenticadoId();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Usuario no encontrado"));

        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);
        compra.setDocumento(request.getDocumento());
        compra.setFechaCompra(LocalDateTime.now());
        compra.setEstado(EstadoCompra.REGISTRADA);
        
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleCompraRequest detReq : request.getDetalles()) {
            Producto producto = productoRepository.findByIdWithLock(detReq.getIdProducto())
                    .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Producto no encontrado: " + detReq.getIdProducto()));
                    
            Lote lote = new Lote();
            lote.setProducto(producto);
            lote.setCodigoLote(detReq.getCodigoLote());
            lote.setFechaVencimiento(detReq.getFechaVencimiento());
            lote.setFechaIngreso(java.time.LocalDate.now());
            lote.setStockInicial(detReq.getCantidad());
            lote.setStockActual(detReq.getCantidad());
            lote.setPrecioCompra(detReq.getCostoUnitario());
            lote.setEstado(EstadoLote.DISPONIBLE);
            lote = loteRepository.save(lote);

            DetalleCompra detCompra = new DetalleCompra();
            detCompra.setCompra(compra);
            detCompra.setProducto(producto);
            detCompra.setLote(lote);
            detCompra.setCantidad(detReq.getCantidad());
            detCompra.setCostoUnitario(detReq.getCostoUnitario());
            
            BigDecimal subtotal = detReq.getCostoUnitario().multiply(new BigDecimal(detReq.getCantidad()));
            detCompra.setSubtotal(subtotal);
            
            compra.getDetalles().add(detCompra);
            total = total.add(subtotal);
            
            producto.setStockActual(producto.getStockActual() + detReq.getCantidad());
            productoRepository.save(producto);
        }
        
        compra.setTotal(total);
        return compraRepository.save(compra);
    }
}
