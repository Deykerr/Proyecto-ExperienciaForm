package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.AjusteInventarioRequest;
import com.vircarmen.botica.dto.ConciliacionStockDTO;
import com.vircarmen.botica.dto.ConciliarStockRequest;
import com.vircarmen.botica.dto.ConteoFisicoDTO;
import com.vircarmen.botica.dto.ConteoFisicoRequest;
import com.vircarmen.botica.dto.IngresoInventarioRequest;
import com.vircarmen.botica.dto.KardexDTO;
import com.vircarmen.botica.entity.DetalleMovimiento;
import com.vircarmen.botica.entity.DetalleVenta;
import com.vircarmen.botica.entity.EstadoConteoInventario;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.EstadoLote;
import com.vircarmen.botica.entity.InventarioConteo;
import com.vircarmen.botica.entity.InventarioConteoDetalle;
import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.entity.Movimiento;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.TipoMovimiento;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.entity.Venta;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.exception.StockInsuficienteException;
import com.vircarmen.botica.repository.DetalleMovimientoRepository;
import com.vircarmen.botica.repository.InventarioConteoRepository;
import com.vircarmen.botica.repository.LoteRepository;
import com.vircarmen.botica.repository.MovimientoRepository;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.SecurityUtils;
import com.vircarmen.botica.service.PrecioVentaService.CalculoPrecio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioService {
    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioConteoRepository inventarioConteoRepository;

    /**
     * Consume lotes con FEFO bajo bloqueo pesimista y deja trazabilidad completa.
     * El Producto recibido debe haberse obtenido con findByIdWithLock.
     */
    public List<DetalleVenta> consumirStockFEFO(
            Producto producto,
            int cantidadRequerida,
            Venta venta,
            CalculoPrecio precio,
            Usuario usuario) {
        validarProductoVendible(producto, cantidadRequerida);

        List<Lote> lotes = loteRepository.findVendiblesFEFOForUpdate(
                producto.getIdProducto(),
                LocalDate.now());
        int disponible = lotes.stream().mapToInt(Lote::getStockActual).sum();
        if (disponible < cantidadRequerida) {
            throw new StockInsuficienteException(
                    "Stock vendible insuficiente para " + producto.getNombre()
                            + ". Requerido: " + cantidadRequerida
                            + ", disponible: " + disponible);
        }

        int stockProducto = stockFisico(producto.getIdProducto());
        if (stockProducto < cantidadRequerida) {
            throw new StockInsuficienteException("El saldo físico del producto es insuficiente.");
        }

        Movimiento movimiento = crearMovimiento(
                TipoMovimiento.VENTA,
                "Salida por venta",
                "VENTA",
                venta.getIdVenta(),
                null,
                usuario);

        List<DetalleVenta> detalles = new ArrayList<>();
        int cantidadRestante = cantidadRequerida;
        int cantidadParaProrrateo = cantidadRequerida;
        BigDecimal totalRestante = precio.total();
        BigDecimal baseRestante = precio.baseImponible();
        BigDecimal igvRestante = precio.igv();

        for (Lote lote : lotes) {
            if (cantidadRestante == 0) {
                break;
            }

            int tomada = Math.min(lote.getStockActual(), cantidadRestante);
            int stockLoteAnterior = lote.getStockActual();
            int stockProductoAnterior = stockProducto;

            lote.setStockActual(stockLoteAnterior - tomada);
            if (lote.getStockActual() == 0) {
                lote.setEstado(EstadoLote.AGOTADO);
            }
            stockProducto -= tomada;
            loteRepository.save(lote);

            BigDecimal subtotalDetalle = prorratear(totalRestante, tomada, cantidadParaProrrateo);
            BigDecimal baseDetalle = prorratear(baseRestante, tomada, cantidadParaProrrateo);
            BigDecimal igvDetalle = cantidadParaProrrateo == tomada
                    ? igvRestante
                    : subtotalDetalle.subtract(baseDetalle);

            DetalleVenta detalleVenta = new DetalleVenta();
            detalleVenta.setVenta(venta);
            detalleVenta.setLote(lote);
            detalleVenta.setCantidad(tomada);
            detalleVenta.setPrecioUnitario(subtotalDetalle.divide(
                    BigDecimal.valueOf(tomada), 4, RoundingMode.HALF_UP));
            detalleVenta.setSubtotal(subtotalDetalle);
            detalleVenta.setBaseImponible(baseDetalle);
            detalleVenta.setIgv(igvDetalle);
            detalleVenta.setTipoAfectacionIgv(precio.tipoAfectacionIgv());
            detalles.add(detalleVenta);

            registrarDetalleMovimiento(
                    movimiento,
                    lote,
                    -tomada,
                    lote.getPrecioCompra(),
                    stockLoteAnterior,
                    lote.getStockActual(),
                    stockProductoAnterior,
                    stockProducto);

            cantidadRestante -= tomada;
            cantidadParaProrrateo -= tomada;
            totalRestante = totalRestante.subtract(subtotalDetalle);
            baseRestante = baseRestante.subtract(baseDetalle);
            igvRestante = igvRestante.subtract(igvDetalle);
        }

        producto.setStockActual(stockProducto);
        productoRepository.save(producto);
        return detalles;
    }

    @Transactional
    public void registrarIngresoManual(IngresoInventarioRequest request) {
        if (movimientoRepository.findByIdempotencyKey(request.idempotencyKey()).isPresent()) {
            return;
        }

        Usuario usuario = usuarioAutenticado();
        Producto producto = productoRepository.findByIdWithLock(request.idProducto())
                .orElseThrow(() -> new BusinessException("Producto no encontrado."));
        Movimiento movimiento = crearMovimiento(
                TipoMovimiento.INGRESO_MANUAL,
                request.motivo().trim(),
                "INGRESO_MANUAL",
                null,
                request.idempotencyKey().trim(),
                usuario);

        registrarEntradaLote(
                producto,
                request.codigoLote(),
                request.fechaVencimiento(),
                request.cantidad(),
                request.costoUnitario(),
                movimiento);
    }

    public Lote registrarEntradaLote(
            Producto producto,
            String codigoLote,
            LocalDate fechaVencimiento,
            int cantidad,
            BigDecimal costoUnitario,
            Movimiento movimiento) {
        if (cantidad <= 0) {
            throw new BusinessException("La cantidad de ingreso debe ser mayor a cero.");
        }
        if (!fechaVencimiento.isAfter(LocalDate.now())) {
            throw new BusinessException("No se puede ingresar un lote vencido.");
        }

        String codigoNormalizado = codigoLote.trim().toUpperCase();
        Lote lote = loteRepository
                .findByProductoIdProductoAndCodigoLote(producto.getIdProducto(), codigoNormalizado)
                .orElseGet(Lote::new);

        boolean nuevo = lote.getIdLote() == null;
        if (nuevo) {
            lote.setProducto(producto);
            lote.setCodigoLote(codigoNormalizado);
            lote.setFechaIngreso(LocalDate.now());
            lote.setFechaVencimiento(fechaVencimiento);
            lote.setStockInicial(0);
            lote.setStockActual(0);
        } else if (!lote.getFechaVencimiento().equals(fechaVencimiento)) {
            throw new BusinessException("El lote ya existe con otra fecha de vencimiento.");
        }

        int stockLoteAnterior = lote.getStockActual();
        int stockProductoAnterior = stockFisico(producto.getIdProducto());
        lote.setStockInicial(lote.getStockInicial() + cantidad);
        lote.setStockActual(stockLoteAnterior + cantidad);
        lote.setPrecioCompra(costoUnitario.setScale(2, RoundingMode.HALF_UP));
        if (lote.getEstado() == null || lote.getEstado() == EstadoLote.AGOTADO) {
            lote.setEstado(EstadoLote.DISPONIBLE);
        }
        lote = loteRepository.save(lote);

        producto.setStockActual(stockProductoAnterior + cantidad);
        productoRepository.save(producto);

        registrarDetalleMovimiento(
                movimiento,
                lote,
                cantidad,
                costoUnitario,
                stockLoteAnterior,
                lote.getStockActual(),
                stockProductoAnterior,
                producto.getStockActual());
        return lote;
    }

    /** Reincorpora al lote exacto vendido y registra el movimiento compensatorio. */
    public void reintegrarStockLote(Integer loteId, int cantidad, Movimiento movimiento) {
        if (cantidad <= 0) {
            throw new BusinessException("La cantidad a reintegrar debe ser mayor a cero.");
        }
        Lote referencia = loteRepository.findById(loteId)
                .orElseThrow(() -> new BusinessException("Lote no encontrado."));
        Producto producto = productoRepository.findByIdWithLock(referencia.getProducto().getIdProducto())
                .orElseThrow(() -> new BusinessException("Producto no encontrado."));
        Lote lote = loteRepository.findByIdWithLock(loteId)
                .orElseThrow(() -> new BusinessException("Lote no encontrado."));

        int stockLoteAnterior = lote.getStockActual();
        int stockProductoAnterior = stockFisico(producto.getIdProducto());
        lote.setStockActual(stockLoteAnterior + cantidad);
        if (lote.getFechaVencimiento().isBefore(LocalDate.now())
                || lote.getFechaVencimiento().isEqual(LocalDate.now())) {
            lote.setEstado(EstadoLote.VENCIDO);
        } else if (lote.getEstado() == EstadoLote.AGOTADO) {
            lote.setEstado(EstadoLote.DISPONIBLE);
        }
        loteRepository.save(lote);
        producto.setStockActual(stockProductoAnterior + cantidad);
        productoRepository.save(producto);
        registrarDetalleMovimiento(movimiento, lote, cantidad, lote.getPrecioCompra(),
                stockLoteAnterior, lote.getStockActual(), stockProductoAnterior, producto.getStockActual());
    }

    /** Retira unidades de un lote por devolución a proveedor. */
    public void retirarStockLote(Integer loteId, int cantidad, Movimiento movimiento) {
        if (cantidad <= 0) {
            throw new BusinessException("La cantidad a retirar debe ser mayor a cero.");
        }
        Lote referencia = loteRepository.findById(loteId)
                .orElseThrow(() -> new BusinessException("Lote no encontrado."));
        Producto producto = productoRepository.findByIdWithLock(referencia.getProducto().getIdProducto())
                .orElseThrow(() -> new BusinessException("Producto no encontrado."));
        Lote lote = loteRepository.findByIdWithLock(loteId)
                .orElseThrow(() -> new BusinessException("Lote no encontrado."));
        if (lote.getStockActual() < cantidad) {
            throw new StockInsuficienteException("El lote " + lote.getCodigoLote()
                    + " no tiene stock suficiente para devolver al proveedor.");
        }
        int stockLoteAnterior = lote.getStockActual();
        int stockProductoAnterior = stockFisico(producto.getIdProducto());
        lote.setStockActual(stockLoteAnterior - cantidad);
        if (lote.getStockActual() == 0) lote.setEstado(EstadoLote.AGOTADO);
        loteRepository.save(lote);
        producto.setStockActual(stockProductoAnterior - cantidad);
        productoRepository.save(producto);
        registrarDetalleMovimiento(movimiento, lote, -cantidad, lote.getPrecioCompra(),
                stockLoteAnterior, lote.getStockActual(), stockProductoAnterior, producto.getStockActual());
    }

    @Transactional
    public void ajustarStock(AjusteInventarioRequest request) {
        Lote referencia = loteRepository.findById(request.idLote())
                .orElseThrow(() -> new BusinessException("Lote no encontrado."));
        Producto producto = productoRepository.findByIdWithLock(referencia.getProducto().getIdProducto())
                .orElseThrow(() -> new BusinessException("Producto no encontrado."));
        Lote lote = loteRepository.findByIdWithLock(request.idLote())
                .orElseThrow(() -> new BusinessException("Lote no encontrado."));

        int diferencia = request.cantidadContada() - lote.getStockActual();
        if (diferencia == 0) {
            throw new BusinessException("El stock contado es igual al stock del sistema; no hay ajuste que registrar.");
        }

        Movimiento movimiento = crearMovimiento(
                diferencia > 0 ? TipoMovimiento.AJUSTE_POSITIVO : TipoMovimiento.AJUSTE_NEGATIVO,
                request.motivo().trim(),
                "AJUSTE",
                lote.getIdLote(),
                null,
                usuarioAutenticado());
        aplicarAjuste(producto, lote, request.cantidadContada(), movimiento);
    }

    @Transactional
    public ConteoFisicoDTO registrarConteo(ConteoFisicoRequest request) {
        Set<Integer> ids = new HashSet<>();
        for (ConteoFisicoRequest.Item item : request.items()) {
            if (!ids.add(item.idLote())) {
                throw new BusinessException("Un lote no puede aparecer dos veces en el mismo conteo.");
            }
        }

        Map<Integer, ConteoFisicoRequest.Item> itemsPorId = new HashMap<>();
        request.items().forEach(item -> itemsPorId.put(item.idLote(), item));
        List<Lote> referencias = loteRepository.findAllById(ids);
        if (referencias.size() != ids.size()) {
            throw new BusinessException("Uno o más lotes no existen.");
        }
        referencias.sort(Comparator
                .comparing((Lote l) -> l.getProducto().getIdProducto())
                .thenComparing(Lote::getIdLote));

        Usuario usuario = usuarioAutenticado();
        LocalDateTime ahora = LocalDateTime.now();
        InventarioConteo conteo = new InventarioConteo();
        conteo.setFechaInicio(ahora);
        conteo.setFechaFinalizacion(ahora);
        conteo.setMotivo(request.motivo().trim());
        conteo.setEstado(EstadoConteoInventario.FINALIZADO);
        conteo.setUsuario(usuario);
        conteo = inventarioConteoRepository.save(conteo);

        Movimiento movimiento = crearMovimiento(
                TipoMovimiento.CONTEO_FISICO,
                request.motivo().trim(),
                "CONTEO_FISICO",
                conteo.getIdConteo(),
                null,
                usuario);

        for (Lote referencia : referencias) {
            Producto producto = productoRepository.findByIdWithLock(referencia.getProducto().getIdProducto())
                    .orElseThrow(() -> new BusinessException("Producto no encontrado."));
            Lote lote = loteRepository.findByIdWithLock(referencia.getIdLote())
                    .orElseThrow(() -> new BusinessException("Lote no encontrado."));
            int stockSistema = lote.getStockActual();
            int stockContado = itemsPorId.get(lote.getIdLote()).cantidadContada();

            InventarioConteoDetalle detalle = new InventarioConteoDetalle();
            detalle.setConteo(conteo);
            detalle.setLote(lote);
            detalle.setStockSistema(stockSistema);
            detalle.setStockContado(stockContado);
            detalle.setDiferencia(stockContado - stockSistema);
            conteo.getDetalles().add(detalle);

            if (stockContado != stockSistema) {
                aplicarAjuste(producto, lote, stockContado, movimiento);
            }
        }

        return mapConteo(inventarioConteoRepository.save(conteo));
    }

    @Transactional(readOnly = true)
    public Page<KardexDTO> obtenerKardex(Integer productoId, Integer loteId, Pageable pageable) {
        return detalleMovimientoRepository.buscarKardex(productoId, loteId, pageable)
                .map(this::mapKardex);
    }

    @Transactional(readOnly = true)
    public List<ConteoFisicoDTO> listarConteos() {
        return inventarioConteoRepository.findAllByOrderByFechaFinalizacionDesc().stream()
                .map(this::mapConteo)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConciliacionStockDTO> obtenerDiferenciasStock() {
        return productoRepository.findAll().stream()
                .map(producto -> {
                    int calculado = stockFisico(producto.getIdProducto());
                    int registrado = producto.getStockActual() == null ? 0 : producto.getStockActual();
                    return new ConciliacionStockDTO(
                            producto.getIdProducto(),
                            producto.getNombre(),
                            registrado,
                            calculado,
                            calculado - registrado);
                })
                .filter(item -> item.diferencia() != 0)
                .toList();
    }

    @Transactional
    public List<ConciliacionStockDTO> conciliarStock(ConciliarStockRequest request) {
        List<ConciliacionStockDTO> diferencias = obtenerDiferenciasStock();
        if (diferencias.isEmpty()) {
            return List.of();
        }

        Usuario usuario = usuarioAutenticado();
        Movimiento movimiento = crearMovimiento(
                TipoMovimiento.CONCILIACION,
                request.motivo().trim(),
                "CONCILIACION",
                null,
                null,
                usuario);

        for (ConciliacionStockDTO diferencia : diferencias) {
            Producto producto = productoRepository.findByIdWithLock(diferencia.idProducto())
                    .orElseThrow(() -> new BusinessException("Producto no encontrado."));
            int anterior = producto.getStockActual() == null ? 0 : producto.getStockActual();
            int calculado = stockFisico(producto.getIdProducto());
            producto.setStockActual(calculado);
            productoRepository.save(producto);
            registrarDetalleConciliacion(movimiento, producto, anterior, calculado);
        }
        return diferencias;
    }

    public Movimiento crearMovimiento(
            TipoMovimiento tipo,
            String motivo,
            String referenciaTipo,
            Integer referenciaId,
            String idempotencyKey,
            Usuario usuario) {
        Movimiento movimiento = new Movimiento();
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setTipoMovimiento(tipo);
        movimiento.setMotivo(motivo);
        movimiento.setReferenciaTipo(referenciaTipo);
        movimiento.setReferenciaId(referenciaId);
        movimiento.setIdempotencyKey(idempotencyKey);
        movimiento.setUsuario(usuario);
        return movimientoRepository.save(movimiento);
    }

    private void aplicarAjuste(Producto producto, Lote lote, int stockContado, Movimiento movimiento) {
        int stockLoteAnterior = lote.getStockActual();
        int stockProductoAnterior = stockFisico(producto.getIdProducto());
        int diferencia = stockContado - stockLoteAnterior;

        lote.setStockActual(stockContado);
        if (stockContado == 0) {
            lote.setEstado(EstadoLote.AGOTADO);
        } else if (lote.getEstado() == EstadoLote.AGOTADO) {
            lote.setEstado(EstadoLote.DISPONIBLE);
        }
        loteRepository.save(lote);

        producto.setStockActual(stockProductoAnterior + diferencia);
        productoRepository.save(producto);
        registrarDetalleMovimiento(
                movimiento,
                lote,
                diferencia,
                lote.getPrecioCompra() == null ? BigDecimal.ZERO : lote.getPrecioCompra(),
                stockLoteAnterior,
                stockContado,
                stockProductoAnterior,
                producto.getStockActual());
    }

    private void registrarDetalleMovimiento(
            Movimiento movimiento,
            Lote lote,
            int cantidad,
            BigDecimal precio,
            int stockLoteAnterior,
            int stockLotePosterior,
            int stockProductoAnterior,
            int stockProductoPosterior) {
        DetalleMovimiento detalle = new DetalleMovimiento();
        detalle.setMovimiento(movimiento);
        detalle.setLote(lote);
        detalle.setProducto(lote.getProducto());
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precio == null ? BigDecimal.ZERO : precio.setScale(2, RoundingMode.HALF_UP));
        detalle.setStockLoteAnterior(stockLoteAnterior);
        detalle.setStockLotePosterior(stockLotePosterior);
        detalle.setStockProductoAnterior(stockProductoAnterior);
        detalle.setStockProductoPosterior(stockProductoPosterior);
        detalleMovimientoRepository.save(detalle);
    }

    private void registrarDetalleConciliacion(
            Movimiento movimiento,
            Producto producto,
            int stockAnterior,
            int stockPosterior) {
        DetalleMovimiento detalle = new DetalleMovimiento();
        detalle.setMovimiento(movimiento);
        detalle.setProducto(producto);
        detalle.setLote(null);
        detalle.setCantidad(stockPosterior - stockAnterior);
        detalle.setPrecioUnitario(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        detalle.setStockLoteAnterior(null);
        detalle.setStockLotePosterior(null);
        detalle.setStockProductoAnterior(stockAnterior);
        detalle.setStockProductoPosterior(stockPosterior);
        detalleMovimientoRepository.save(detalle);
    }

    private int stockFisico(Integer productoId) {
        return Math.toIntExact(loteRepository.sumStockByProductoId(productoId));
    }

    private BigDecimal prorratear(BigDecimal restante, int cantidad, int cantidadRestante) {
        if (cantidad == cantidadRestante) {
            return restante;
        }
        return restante.multiply(BigDecimal.valueOf(cantidad))
                .divide(BigDecimal.valueOf(cantidadRestante), 2, RoundingMode.HALF_UP);
    }

    private void validarProductoVendible(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a cero.");
        }
        if (producto.getEstado() != EstadoGeneral.A) {
            throw new BusinessException("El producto " + producto.getNombre() + " está inactivo.");
        }
    }

    private Usuario usuarioAutenticado() {
        Integer idUsuario = SecurityUtils.getUsuarioAutenticadoId();
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado."));
    }

    private KardexDTO mapKardex(DetalleMovimiento detalle) {
        Movimiento movimiento = detalle.getMovimiento();
        Lote lote = detalle.getLote();
        Producto producto = detalle.getProducto();
        return new KardexDTO(
                detalle.getIdDetalle(),
                movimiento.getFechaMovimiento(),
                movimiento.getTipoMovimiento().name(),
                producto.getIdProducto(),
                producto.getNombre(),
                lote == null ? null : lote.getIdLote(),
                lote == null ? null : lote.getCodigoLote(),
                detalle.getCantidad(),
                detalle.getStockLoteAnterior(),
                detalle.getStockLotePosterior(),
                detalle.getStockProductoAnterior(),
                detalle.getStockProductoPosterior(),
                detalle.getPrecioUnitario(),
                movimiento.getReferenciaTipo(),
                movimiento.getReferenciaId(),
                movimiento.getMotivo(),
                movimiento.getUsuario().getUsername());
    }

    private ConteoFisicoDTO mapConteo(InventarioConteo conteo) {
        return new ConteoFisicoDTO(
                conteo.getIdConteo(),
                conteo.getFechaInicio(),
                conteo.getFechaFinalizacion(),
                conteo.getMotivo(),
                conteo.getEstado().name(),
                conteo.getUsuario().getUsername(),
                conteo.getDetalles().stream()
                        .map(detalle -> new ConteoFisicoDTO.Item(
                                detalle.getLote().getIdLote(),
                                detalle.getLote().getCodigoLote(),
                                detalle.getLote().getProducto().getNombre(),
                                detalle.getStockSistema(),
                                detalle.getStockContado(),
                                detalle.getDiferencia()))
                        .toList());
    }
}
