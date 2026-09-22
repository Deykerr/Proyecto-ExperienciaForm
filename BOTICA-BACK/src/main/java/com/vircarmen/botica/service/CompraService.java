package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.CompraDTO;
import com.vircarmen.botica.dto.CompraRequest;
import com.vircarmen.botica.dto.DetalleCompraRequest;
import com.vircarmen.botica.dto.DevolucionProveedorDTO;
import com.vircarmen.botica.dto.DevolucionProveedorRequest;
import com.vircarmen.botica.dto.RecepcionCompraDTO;
import com.vircarmen.botica.dto.RecepcionCompraRequest;
import com.vircarmen.botica.entity.Compra;
import com.vircarmen.botica.entity.DetalleCompra;
import com.vircarmen.botica.entity.DevolucionProveedor;
import com.vircarmen.botica.entity.DevolucionProveedorDetalle;
import com.vircarmen.botica.entity.EstadoCompra;
import com.vircarmen.botica.entity.EstadoDevolucionProveedor;
import com.vircarmen.botica.entity.EstadoRecepcionCompra;
import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.entity.Movimiento;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.Proveedor;
import com.vircarmen.botica.entity.RecepcionCompra;
import com.vircarmen.botica.entity.RecepcionCompraDetalle;
import com.vircarmen.botica.entity.TipoMovimiento;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.CompraRepository;
import com.vircarmen.botica.repository.DetalleCompraRepository;
import com.vircarmen.botica.repository.DevolucionProveedorRepository;
import com.vircarmen.botica.repository.LoteRepository;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.ProveedorRepository;
import com.vircarmen.botica.repository.RecepcionCompraRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompraService {
    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final RecepcionCompraRepository recepcionRepository;
    private final DevolucionProveedorRepository devolucionProveedorRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final InventarioService inventarioService;

    /** Registra la orden/documento comercial. El stock cambia únicamente al recibir. */
    @Transactional
    public CompraDTO registrarCompra(CompraRequest request) {
        String key = request.getIdempotencyKey().trim();
        Compra existente = compraRepository.findByIdempotencyKey(key).orElse(null);
        if (existente != null) return mapCompra(existente);

        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
                .orElseThrow(() -> new BusinessException("Proveedor no encontrado."));
        String documento = request.getDocumento().trim().toUpperCase();
        if (compraRepository.existsByProveedorIdProveedorAndDocumentoIgnoreCase(
                proveedor.getIdProveedor(), documento)) {
            throw new BusinessException("El documento ya fue registrado para este proveedor.");
        }
        validarDetallesSinDuplicados(request.getDetalles());
        Usuario usuario = usuarioAutenticado();

        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);
        compra.setDocumento(documento);
        compra.setFechaCompra(LocalDateTime.now());
        compra.setFechaEsperada(request.getFechaEsperada());
        compra.setObservaciones(normalizar(request.getObservaciones()));
        compra.setEstado(EstadoCompra.PENDIENTE_RECEPCION);
        compra.setIdempotencyKey(key);

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCompraRequest item : request.getDetalles()) {
            Producto producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new BusinessException("Producto no encontrado: " + item.getIdProducto()));
            BigDecimal costo = item.getCostoUnitario().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = costo.multiply(BigDecimal.valueOf(item.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);
            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setLote(null);
            detalle.setCantidad(item.getCantidad());
            detalle.setCantidadRecibida(0);
            detalle.setCostoUnitario(costo);
            detalle.setSubtotal(subtotal);
            compra.getDetalles().add(detalle);
            total = total.add(subtotal);
        }
        compra.setTotal(total);
        return mapCompra(compraRepository.save(compra));
    }

    @Transactional
    public RecepcionCompraDTO registrarRecepcion(Integer compraId, RecepcionCompraRequest request) {
        RecepcionCompra existente = recepcionRepository.findByIdempotencyKey(request.idempotencyKey().trim()).orElse(null);
        if (existente != null) return mapRecepcion(existente);

        Compra compra = compraRepository.findByIdWithLock(compraId)
                .orElseThrow(() -> new BusinessException("Compra no encontrada."));
        if (compra.getEstado() == EstadoCompra.ANULADA) {
            throw new BusinessException("No se puede recibir una compra anulada.");
        }
        if (compra.getEstado() == EstadoCompra.RECIBIDA) {
            throw new BusinessException("La compra ya fue recibida completamente.");
        }
        validarIdsUnicos(request.detalles().stream().map(RecepcionCompraRequest.Item::idDetalleCompra).toList(),
                "Un detalle de compra no puede repetirse en la recepción.");

        Usuario usuario = usuarioAutenticado();
        RecepcionCompra recepcion = new RecepcionCompra();
        recepcion.setCompra(compra);
        recepcion.setDocumentoProveedor(normalizar(request.documentoProveedor()));
        recepcion.setObservaciones(normalizar(request.observaciones()));
        recepcion.setUsuario(usuario);
        recepcion.setFecha(LocalDateTime.now());
        recepcion.setEstado(EstadoRecepcionCompra.CONFIRMADA);
        recepcion.setIdempotencyKey(request.idempotencyKey().trim());
        recepcion = recepcionRepository.saveAndFlush(recepcion);

        Movimiento movimiento = inventarioService.crearMovimiento(
                TipoMovimiento.RECEPCION_COMPRA,
                "Recepción de compra " + compra.getDocumento(),
                "RECEPCION_COMPRA", recepcion.getIdRecepcion(), null, usuario);
        movimiento.setProveedor(compra.getProveedor());

        List<RecepcionCompraRequest.Item> ordenados = request.detalles().stream()
                .sorted(Comparator.comparing(item -> detalleCompraRepository
                        .findByIdDetalleCompraAndCompraIdCompra(item.idDetalleCompra(), compraId)
                        .map(d -> d.getProducto().getIdProducto()).orElse(Integer.MAX_VALUE)))
                .toList();

        for (RecepcionCompraRequest.Item item : ordenados) {
            DetalleCompra detalle = detalleCompraRepository
                    .findByIdDetalleCompraAndCompraIdCompra(item.idDetalleCompra(), compraId)
                    .orElseThrow(() -> new BusinessException("Detalle de compra no encontrado."));
            int pendiente = detalle.getCantidad() - detalle.getCantidadRecibida();
            if (item.cantidad() > pendiente) {
                throw new BusinessException("La recepción supera la cantidad pendiente de "
                        + detalle.getProducto().getNombre() + ". Pendiente: " + pendiente);
            }
            Producto producto = productoRepository.findByIdWithLock(detalle.getProducto().getIdProducto())
                    .orElseThrow(() -> new BusinessException("Producto no encontrado."));
            BigDecimal costo = item.costoUnitario() == null
                    ? detalle.getCostoUnitario()
                    : item.costoUnitario().setScale(2, RoundingMode.HALF_UP);
            if (detalle.getCantidadRecibida() > 0 && costo.compareTo(detalle.getCostoUnitario()) != 0) {
                throw new BusinessException("El costo de " + detalle.getProducto().getNombre()
                        + " no puede cambiar después de la primera recepción.");
            }
            if (detalle.getCantidadRecibida() == 0 && costo.compareTo(detalle.getCostoUnitario()) != 0) {
                detalle.setCostoUnitario(costo);
                detalle.setSubtotal(costo.multiply(BigDecimal.valueOf(detalle.getCantidad()))
                        .setScale(2, RoundingMode.HALF_UP));
            }
            Lote lote = inventarioService.registrarEntradaLote(producto, item.codigoLote(),
                    item.fechaVencimiento(), item.cantidad(), costo, movimiento);

            detalle.setCantidadRecibida(detalle.getCantidadRecibida() + item.cantidad());
            if (detalle.getLote() == null) detalle.setLote(lote); // compatibilidad con datos previos
            detalleCompraRepository.save(detalle);

            RecepcionCompraDetalle recibido = new RecepcionCompraDetalle();
            recibido.setRecepcion(recepcion);
            recibido.setDetalleCompra(detalle);
            recibido.setLote(lote);
            recibido.setCantidad(item.cantidad());
            recibido.setCostoUnitario(costo);
            recepcion.getDetalles().add(recibido);
        }
        actualizarEstado(compra);
        compra.setTotal(compra.getDetalles().stream().map(DetalleCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP));
        compraRepository.save(compra);
        return mapRecepcion(recepcionRepository.save(recepcion));
    }

    @Transactional
    public DevolucionProveedorDTO devolverProveedor(Integer compraId, DevolucionProveedorRequest request) {
        DevolucionProveedor existente = devolucionProveedorRepository
                .findByIdempotencyKey(request.idempotencyKey().trim()).orElse(null);
        if (existente != null) return mapDevolucion(existente);

        Compra compra = compraRepository.findByIdWithLock(compraId)
                .orElseThrow(() -> new BusinessException("Compra no encontrada."));
        if (compra.getEstado() == EstadoCompra.PENDIENTE_RECEPCION || compra.getEstado() == EstadoCompra.ANULADA) {
            throw new BusinessException("La compra no tiene mercadería recibida disponible para devolución.");
        }
        validarParesDetalleLoteUnicos(request.detalles());
        Usuario usuario = usuarioAutenticado();

        DevolucionProveedor devolucion = new DevolucionProveedor();
        devolucion.setCompra(compra);
        devolucion.setProveedor(compra.getProveedor());
        devolucion.setUsuario(usuario);
        devolucion.setFecha(LocalDateTime.now());
        devolucion.setMotivo(request.motivo().trim());
        devolucion.setDocumentoReferencia(normalizar(request.documentoReferencia()));
        devolucion.setEstado(EstadoDevolucionProveedor.CONFIRMADA);
        devolucion.setIdempotencyKey(request.idempotencyKey().trim());
        devolucion.setTotal(BigDecimal.ZERO.setScale(2));
        devolucion = devolucionProveedorRepository.saveAndFlush(devolucion);

        Movimiento movimiento = inventarioService.crearMovimiento(
                TipoMovimiento.DEVOLUCION_PROVEEDOR, request.motivo().trim(),
                "DEVOLUCION_PROVEEDOR", devolucion.getIdDevolucionProveedor(), null, usuario);
        movimiento.setProveedor(compra.getProveedor());

        BigDecimal total = BigDecimal.ZERO;
        List<DevolucionProveedorRequest.Item> ordenados = request.detalles().stream()
                .sorted(Comparator.comparing(DevolucionProveedorRequest.Item::idLote)).toList();
        for (DevolucionProveedorRequest.Item item : ordenados) {
            DetalleCompra detalle = detalleCompraRepository
                    .findByIdDetalleCompraAndCompraIdCompra(item.idDetalleCompra(), compraId)
                    .orElseThrow(() -> new BusinessException("Detalle de compra no encontrado."));
            long recibido = detalleCompraRepository.sumCantidadRecibidaPorLote(item.idDetalleCompra(), item.idLote());
            long devuelto = detalleCompraRepository.sumCantidadDevueltaPorLote(item.idDetalleCompra(), item.idLote());
            if (item.cantidad() > recibido - devuelto) {
                throw new BusinessException("La devolución supera lo recibido del lote para "
                        + detalle.getProducto().getNombre() + ".");
            }
            Lote lote = loteRepository.findByIdWithLock(item.idLote())
                    .orElseThrow(() -> new BusinessException("Lote no encontrado."));
            if (!lote.getProducto().getIdProducto().equals(detalle.getProducto().getIdProducto())) {
                throw new BusinessException("El lote no pertenece al producto comprado.");
            }
            inventarioService.retirarStockLote(lote.getIdLote(), item.cantidad(), movimiento);
            BigDecimal subtotal = detalle.getCostoUnitario().multiply(BigDecimal.valueOf(item.cantidad()))
                    .setScale(2, RoundingMode.HALF_UP);
            DevolucionProveedorDetalle linea = new DevolucionProveedorDetalle();
            linea.setDevolucionProveedor(devolucion);
            linea.setDetalleCompra(detalle);
            linea.setLote(lote);
            linea.setCantidad(item.cantidad());
            linea.setCostoUnitario(detalle.getCostoUnitario());
            linea.setSubtotal(subtotal);
            devolucion.getDetalles().add(linea);
            total = total.add(subtotal);
        }
        devolucion.setTotal(total);
        return mapDevolucion(devolucionProveedorRepository.save(devolucion));
    }

    @Transactional(readOnly = true)
    public List<CompraDTO> listar() {
        return compraRepository.findAll().stream()
                .sorted(Comparator.comparing(Compra::getFechaCompra).reversed())
                .map(this::mapCompra).toList();
    }

    @Transactional(readOnly = true)
    public CompraDTO obtener(Integer id) {
        return mapCompra(compraRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Compra no encontrada.")));
    }

    @Transactional(readOnly = true)
    public List<RecepcionCompraDTO> listarRecepciones(Integer compraId) {
        if (!compraRepository.existsById(compraId)) throw new BusinessException("Compra no encontrada.");
        return recepcionRepository.findByCompraIdCompraOrderByFechaDesc(compraId).stream()
                .map(this::mapRecepcion).toList();
    }

    @Transactional(readOnly = true)
    public List<DevolucionProveedorDTO> listarDevoluciones(Integer compraId) {
        if (!compraRepository.existsById(compraId)) throw new BusinessException("Compra no encontrada.");
        return devolucionProveedorRepository.findByCompraIdCompraOrderByFechaDesc(compraId).stream()
                .map(this::mapDevolucion).toList();
    }

    private void actualizarEstado(Compra compra) {
        boolean completa = compra.getDetalles().stream()
                .allMatch(d -> d.getCantidadRecibida() >= d.getCantidad());
        boolean alguna = compra.getDetalles().stream().anyMatch(d -> d.getCantidadRecibida() > 0);
        compra.setEstado(completa ? EstadoCompra.RECIBIDA
                : alguna ? EstadoCompra.RECIBIDA_PARCIAL : EstadoCompra.PENDIENTE_RECEPCION);
    }

    private void validarDetallesSinDuplicados(List<DetalleCompraRequest> detalles) {
        validarIdsUnicos(detalles.stream().map(DetalleCompraRequest::getIdProducto).toList(),
                "El mismo producto no puede aparecer más de una vez en la compra.");
    }

    private void validarIdsUnicos(List<Integer> ids, String mensaje) {
        Set<Integer> unicos = new HashSet<>();
        ids.forEach(id -> {
            if (!unicos.add(id)) throw new BusinessException(mensaje);
        });
    }

    private void validarParesDetalleLoteUnicos(List<DevolucionProveedorRequest.Item> detalles) {
        Set<String> unicos = new HashSet<>();
        detalles.forEach(item -> {
            String clave = item.idDetalleCompra() + ":" + item.idLote();
            if (!unicos.add(clave)) {
                throw new BusinessException("El mismo detalle y lote no puede repetirse en la devolución.");
            }
        });
    }

    private Usuario usuarioAutenticado() {
        return usuarioRepository.findById(SecurityUtils.getUsuarioAutenticadoId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private CompraDTO mapCompra(Compra c) {
        return new CompraDTO(c.getIdCompra(), c.getProveedor().getIdProveedor(), c.getProveedor().getRazonSocial(),
                c.getDocumento(), c.getFechaCompra(), c.getFechaEsperada(), c.getObservaciones(), c.getTotal(),
                c.getEstado().name(), c.getDetalles().stream().map(d -> new CompraDTO.Item(
                        d.getIdDetalleCompra(), d.getProducto().getIdProducto(), d.getProducto().getNombre(),
                        d.getCantidad(), d.getCantidadRecibida(), d.getCantidad() - d.getCantidadRecibida(),
                        d.getCostoUnitario(), d.getSubtotal())).toList());
    }

    private RecepcionCompraDTO mapRecepcion(RecepcionCompra r) {
        return new RecepcionCompraDTO(r.getIdRecepcion(), r.getCompra().getIdCompra(), r.getDocumentoProveedor(),
                r.getFecha(), r.getEstado().name(), r.getObservaciones(), r.getDetalles().stream()
                .map(d -> new RecepcionCompraDTO.Item(d.getDetalleCompra().getIdDetalleCompra(),
                        d.getDetalleCompra().getProducto().getIdProducto(), d.getDetalleCompra().getProducto().getNombre(),
                        d.getLote().getIdLote(), d.getLote().getCodigoLote(), d.getCantidad(), d.getCostoUnitario()))
                .toList());
    }

    private DevolucionProveedorDTO mapDevolucion(DevolucionProveedor d) {
        return new DevolucionProveedorDTO(d.getIdDevolucionProveedor(), d.getCompra().getIdCompra(),
                d.getProveedor().getIdProveedor(), d.getFecha(), d.getMotivo(), d.getDocumentoReferencia(),
                d.getEstado().name(), d.getTotal(), d.getDetalles().stream().map(item ->
                        new DevolucionProveedorDTO.Item(item.getDetalleCompra().getIdDetalleCompra(),
                                item.getDetalleCompra().getProducto().getIdProducto(),
                                item.getDetalleCompra().getProducto().getNombre(), item.getLote().getIdLote(),
                                item.getLote().getCodigoLote(), item.getCantidad(), item.getCostoUnitario(),
                                item.getSubtotal())).toList());
    }
}
