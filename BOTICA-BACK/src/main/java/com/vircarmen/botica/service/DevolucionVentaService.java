package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.AnulacionVentaRequest;
import com.vircarmen.botica.dto.DevolucionVentaDTO;
import com.vircarmen.botica.dto.DevolucionVentaRequest;
import com.vircarmen.botica.entity.CajaSesion;
import com.vircarmen.botica.entity.DetalleVenta;
import com.vircarmen.botica.entity.DevolucionVenta;
import com.vircarmen.botica.entity.DevolucionVentaDetalle;
import com.vircarmen.botica.entity.EstadoDevolucion;
import com.vircarmen.botica.entity.EstadoPago;
import com.vircarmen.botica.entity.EstadoVenta;
import com.vircarmen.botica.entity.MetodoPago;
import com.vircarmen.botica.entity.Movimiento;
import com.vircarmen.botica.entity.MovimientoCaja;
import com.vircarmen.botica.entity.ReembolsoVenta;
import com.vircarmen.botica.entity.TipoDevolucionVenta;
import com.vircarmen.botica.entity.TipoMovimiento;
import com.vircarmen.botica.entity.TipoMovimientoCaja;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.entity.Venta;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.exception.CajaNoAbiertaException;
import com.vircarmen.botica.repository.CajaSesionRepository;
import com.vircarmen.botica.repository.DetalleVentaRepository;
import com.vircarmen.botica.repository.DevolucionVentaRepository;
import com.vircarmen.botica.repository.MovimientoCajaRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.repository.VentaRepository;
import com.vircarmen.botica.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DevolucionVentaService {
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final DevolucionVentaRepository devolucionRepository;
    private final CajaSesionRepository cajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioService inventarioService;
    private final RecetaService recetaService;
    private final DocumentoElectronicoService documentoElectronicoService;

    @Transactional
    public DevolucionVentaDTO devolver(Integer ventaId, DevolucionVentaRequest request) {
        return procesar(ventaId, request.idempotencyKey(), request.motivo(), request.items(),
                request.metodoReembolso(), request.referenciaReembolso(), request.reembolsos(), null);
    }

    @Transactional
    public DevolucionVentaDTO anular(Integer ventaId, AnulacionVentaRequest request) {
        DevolucionVenta existente = devolucionRepository
                .findByIdempotencyKey(request.idempotencyKey().trim()).orElse(null);
        if (existente != null) return map(existente);
        Venta venta = ventaRepository.findByIdWithLock(ventaId)
                .orElseThrow(() -> new BusinessException("Venta no encontrada."));
        if (venta.getEstado() != EstadoVenta.PAGADA) {
            throw new BusinessException("Solo se puede anular una venta pagada sin devoluciones previas.");
        }
        List<DevolucionVentaRequest.Item> items = venta.getDetalles().stream()
                .map(d -> new DevolucionVentaRequest.Item(d.getIdDetalleVenta(), d.getCantidad())).toList();
        return procesar(ventaId, request.idempotencyKey(), request.motivo(), items,
                request.metodoReembolso(), request.referenciaReembolso(), request.reembolsos(),
                TipoDevolucionVenta.ANULACION);
    }

    @Transactional(readOnly = true)
    public List<DevolucionVentaDTO> listarPorVenta(Integer ventaId) {
        return devolucionRepository.findByVentaIdVentaOrderByFechaDesc(ventaId).stream().map(this::map).toList();
    }

    private DevolucionVentaDTO procesar(
            Integer ventaId, String key, String motivo, List<DevolucionVentaRequest.Item> items,
            String metodoLegacy, String referenciaLegacy, List<DevolucionVentaRequest.Reembolso> reembolsosRequest,
            TipoDevolucionVenta tipoForzado) {
        DevolucionVenta existente = devolucionRepository.findByIdempotencyKey(key.trim()).orElse(null);
        if (existente != null) return map(existente);

        Venta venta = ventaRepository.findByIdWithLock(ventaId)
                .orElseThrow(() -> new BusinessException("Venta no encontrada."));
        if (venta.getEstado() == EstadoVenta.ANULADA || venta.getEstado() == EstadoVenta.DEVUELTA_TOTAL
                || venta.getEstado() == EstadoVenta.PENDIENTE) {
            throw new BusinessException("El estado de la venta no permite devoluciones: " + venta.getEstado());
        }
        validarItemsUnicos(items);
        Usuario usuario = usuarioAutenticado();
        CajaSesion caja = cajaRepository.findByUsuarioIdUsuarioAndEstado(
                        usuario.getIdUsuario(), CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new CajaNoAbiertaException("Debe abrir una caja para registrar el reembolso."));

        List<Linea> lineas = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal base = BigDecimal.ZERO;
        BigDecimal igv = BigDecimal.ZERO;
        for (DevolucionVentaRequest.Item item : items) {
            DetalleVenta detalle = detalleVentaRepository
                    .findByIdDetalleVentaAndVentaIdVenta(item.idDetalleVenta(), ventaId)
                    .orElseThrow(() -> new BusinessException("Detalle de venta no encontrado."));
            int previo = Math.toIntExact(detalleVentaRepository.sumCantidadDevuelta(detalle.getIdDetalleVenta()));
            int disponible = detalle.getCantidad() - previo;
            if (item.cantidad() > disponible) {
                throw new BusinessException("La cantidad a devolver supera el saldo para "
                        + detalle.getLote().getProducto().getNombre() + ". Disponible: " + disponible);
            }
            BigDecimal subtotalLinea = prorrateoIncremental(detalle.getSubtotal(), detalle.getCantidad(), previo, item.cantidad());
            BigDecimal baseLinea = prorrateoIncremental(detalle.getBaseImponible(), detalle.getCantidad(), previo, item.cantidad());
            BigDecimal igvLinea = subtotalLinea.subtract(baseLinea).setScale(2, RoundingMode.HALF_UP);
            lineas.add(new Linea(detalle, item.cantidad(), subtotalLinea, baseLinea, igvLinea));
            subtotal = subtotal.add(subtotalLinea);
            base = base.add(baseLinea);
            igv = igv.add(igvLinea);
        }
        BigDecimal total = subtotal.setScale(2, RoundingMode.HALF_UP);
        List<ReembolsoNormalizado> reembolsos = normalizarReembolsos(
                reembolsosRequest, metodoLegacy, referenciaLegacy, total);
        validarCoberturaReembolso(venta, reembolsos);
        validarEfectivoDisponible(caja, reembolsos);

        boolean devolucionCompleta = venta.getDetalles().stream().allMatch(d -> {
            int previo = Math.toIntExact(detalleVentaRepository.sumCantidadDevuelta(d.getIdDetalleVenta()));
            int actual = items.stream().filter(i -> i.idDetalleVenta().equals(d.getIdDetalleVenta()))
                    .mapToInt(DevolucionVentaRequest.Item::cantidad).sum();
            return previo + actual == d.getCantidad();
        });
        TipoDevolucionVenta tipo = tipoForzado != null ? tipoForzado
                : devolucionCompleta ? TipoDevolucionVenta.TOTAL : TipoDevolucionVenta.PARCIAL;

        DevolucionVenta devolucion = new DevolucionVenta();
        devolucion.setVenta(venta);
        devolucion.setCajaSesion(caja);
        devolucion.setUsuario(usuario);
        devolucion.setFecha(LocalDateTime.now());
        devolucion.setMotivo(motivo.trim());
        devolucion.setTipo(tipo);
        devolucion.setEstado(EstadoDevolucion.CONFIRMADA);
        devolucion.setSubtotal(base.setScale(2, RoundingMode.HALF_UP));
        devolucion.setIgv(igv.setScale(2, RoundingMode.HALF_UP));
        devolucion.setTotal(total);
        devolucion.setIdempotencyKey(key.trim());
        if (reembolsos.size() == 1) {
            devolucion.setMetodoReembolso(reembolsos.getFirst().metodo());
            devolucion.setReferenciaReembolso(reembolsos.getFirst().referencia());
        }
        devolucion = devolucionRepository.saveAndFlush(devolucion);

        for (ReembolsoNormalizado item : reembolsos) {
            ReembolsoVenta reembolso = new ReembolsoVenta();
            reembolso.setDevolucionVenta(devolucion);
            reembolso.setMetodoPago(item.metodo());
            reembolso.setMonto(item.monto());
            reembolso.setReferencia(item.referencia());
            devolucion.getReembolsos().add(reembolso);
        }

        Movimiento movimiento = inventarioService.crearMovimiento(
                tipo == TipoDevolucionVenta.ANULACION ? TipoMovimiento.ANULACION : TipoMovimiento.DEVOLUCION_VENTA,
                motivo.trim(), tipo == TipoDevolucionVenta.ANULACION ? "ANULACION_VENTA" : "DEVOLUCION_VENTA",
                devolucion.getIdDevolucionVenta(), null, usuario);
        Map<Integer, Integer> cantidadesReceta = new HashMap<>();
        for (Linea linea : lineas) {
            inventarioService.reintegrarStockLote(linea.detalle().getLote().getIdLote(), linea.cantidad(), movimiento);
            DevolucionVentaDetalle detalle = new DevolucionVentaDetalle();
            detalle.setDevolucionVenta(devolucion);
            detalle.setDetalleVenta(linea.detalle());
            detalle.setLote(linea.detalle().getLote());
            detalle.setCantidad(linea.cantidad());
            detalle.setSubtotal(linea.subtotal());
            detalle.setBaseImponible(linea.base());
            detalle.setIgv(linea.igv());
            devolucion.getDetalles().add(detalle);
            cantidadesReceta.merge(linea.detalle().getLote().getProducto().getIdProducto(), linea.cantidad(), Integer::sum);
        }
        recetaService.revertirDispensacion(venta.getReceta(), cantidadesReceta);

        BigDecimal efectivo = reembolsos.stream().filter(r -> r.metodo() == MetodoPago.EFECTIVO)
                .map(ReembolsoNormalizado::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (efectivo.signum() > 0) {
            MovimientoCaja salida = new MovimientoCaja();
            salida.setCajaSesion(caja);
            salida.setTipoMovimiento(TipoMovimientoCaja.EGRESO);
            salida.setMonto(efectivo);
            salida.setMotivo("Reembolso de venta #" + ventaId + ": " + motivo.trim());
            salida.setFecha(LocalDateTime.now());
            salida.setUsuario(usuario);
            salida.setReferenciaTipo("DEVOLUCION_VENTA");
            salida.setReferenciaId(devolucion.getIdDevolucionVenta());
            salida.setIdempotencyKey(keyCaja(key));
            movimientoCajaRepository.save(salida);
            caja.getMovimientos().add(salida);
        }

        venta.setEstado(tipo == TipoDevolucionVenta.ANULACION ? EstadoVenta.ANULADA
                : devolucionCompleta ? EstadoVenta.DEVUELTA_TOTAL : EstadoVenta.DEVUELTA_PARCIAL);
        ventaRepository.save(venta);
        DevolucionVenta guardada = devolucionRepository.save(devolucion);
        documentoElectronicoService.encolarNotaCredito(guardada);
        return map(guardada);
    }

    private List<ReembolsoNormalizado> normalizarReembolsos(
            List<DevolucionVentaRequest.Reembolso> items, String metodoLegacy, String referenciaLegacy,
            BigDecimal total) {
        List<ReembolsoNormalizado> resultado = new ArrayList<>();
        if (items != null && !items.isEmpty()) {
            for (DevolucionVentaRequest.Reembolso item : items) {
                resultado.add(new ReembolsoNormalizado(parseMetodo(item.metodoPago()),
                        item.monto().setScale(2, RoundingMode.HALF_UP), normalizar(item.referencia())));
            }
        } else {
            if (metodoLegacy == null || metodoLegacy.isBlank()) {
                throw new BusinessException("Debe indicar al menos un medio de reembolso.");
            }
            resultado.add(new ReembolsoNormalizado(parseMetodo(metodoLegacy), total, normalizar(referenciaLegacy)));
        }
        Set<MetodoPago> metodos = new HashSet<>();
        BigDecimal suma = BigDecimal.ZERO;
        for (ReembolsoNormalizado item : resultado) {
            if (!metodos.add(item.metodo())) {
                throw new BusinessException("Un medio de reembolso no puede repetirse.");
            }
            if (item.metodo() != MetodoPago.EFECTIVO && item.referencia() == null) {
                throw new BusinessException("La referencia es obligatoria para reembolsos no efectivos.");
            }
            suma = suma.add(item.monto());
        }
        if (suma.compareTo(total) != 0) {
            throw new BusinessException("Los reembolsos deben sumar exactamente " + total + ".");
        }
        return resultado;
    }

    private void validarCoberturaReembolso(Venta venta, List<ReembolsoNormalizado> nuevos) {
        Map<MetodoPago, BigDecimal> pagado = new EnumMap<>(MetodoPago.class);
        venta.getPagos().stream().filter(p -> p.getEstado() == EstadoPago.APROBADO)
                .forEach(p -> pagado.merge(p.getMetodoPago(), p.getMonto(), BigDecimal::add));
        Map<MetodoPago, BigDecimal> reembolsado = new EnumMap<>(MetodoPago.class);
        devolucionRepository.findByVentaIdVentaOrderByFechaDesc(venta.getIdVenta()).stream()
                .filter(d -> d.getEstado() == EstadoDevolucion.CONFIRMADA)
                .forEach(d -> {
                    if (!d.getReembolsos().isEmpty()) {
                        d.getReembolsos().forEach(r -> reembolsado.merge(r.getMetodoPago(), r.getMonto(), BigDecimal::add));
                    } else if (d.getMetodoReembolso() != null) {
                        reembolsado.merge(d.getMetodoReembolso(), d.getTotal(), BigDecimal::add);
                    }
                });
        for (ReembolsoNormalizado item : nuevos) {
            BigDecimal disponible = pagado.getOrDefault(item.metodo(), BigDecimal.ZERO)
                    .subtract(reembolsado.getOrDefault(item.metodo(), BigDecimal.ZERO));
            if (item.monto().compareTo(disponible) > 0) {
                throw new BusinessException("El reembolso por " + item.metodo()
                        + " supera lo pagado por ese medio. Disponible: " + disponible + ".");
            }
        }
    }

    private void validarEfectivoDisponible(CajaSesion caja, List<ReembolsoNormalizado> reembolsos) {
        BigDecimal devolver = reembolsos.stream().filter(r -> r.metodo() == MetodoPago.EFECTIVO)
                .map(ReembolsoNormalizado::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (devolver.signum() == 0) return;
        BigDecimal cobros = caja.getPagos().stream()
                .filter(p -> p.getEstado() == EstadoPago.APROBADO && p.getMetodoPago() == MetodoPago.EFECTIVO)
                .map(p -> p.getMonto()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ingresos = caja.getMovimientos().stream().filter(m -> m.getTipoMovimiento() == TipoMovimientoCaja.INGRESO)
                .map(MovimientoCaja::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal egresos = caja.getMovimientos().stream().filter(m -> m.getTipoMovimiento() == TipoMovimientoCaja.EGRESO)
                .map(MovimientoCaja::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal disponible = caja.getMontoInicial().add(cobros).add(ingresos).subtract(egresos);
        if (devolver.compareTo(disponible) > 0) {
            throw new BusinessException("La caja no tiene efectivo suficiente para el reembolso. Disponible: " + disponible + ".");
        }
    }

    private BigDecimal prorrateoIncremental(BigDecimal total, int cantidadTotal, int previo, int actual) {
        if (previo + actual == cantidadTotal) {
            BigDecimal yaAsignado = total.multiply(BigDecimal.valueOf(previo))
                    .divide(BigDecimal.valueOf(cantidadTotal), 2, RoundingMode.HALF_UP);
            return total.subtract(yaAsignado).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal acumuladoNuevo = total.multiply(BigDecimal.valueOf(previo + actual))
                .divide(BigDecimal.valueOf(cantidadTotal), 2, RoundingMode.HALF_UP);
        BigDecimal acumuladoPrevio = total.multiply(BigDecimal.valueOf(previo))
                .divide(BigDecimal.valueOf(cantidadTotal), 2, RoundingMode.HALF_UP);
        return acumuladoNuevo.subtract(acumuladoPrevio).setScale(2, RoundingMode.HALF_UP);
    }

    private void validarItemsUnicos(List<DevolucionVentaRequest.Item> items) {
        Set<Integer> ids = new HashSet<>();
        items.forEach(item -> {
            if (!ids.add(item.idDetalleVenta())) {
                throw new BusinessException("Un detalle de venta no puede repetirse en la devolución.");
            }
        });
    }

    private MetodoPago parseMetodo(String valor) {
        try {
            return MetodoPago.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException("Medio de reembolso no válido: " + valor);
        }
    }

    private Usuario usuarioAutenticado() {
        return usuarioRepository.findById(SecurityUtils.getUsuarioAutenticadoId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
    }

    private String keyCaja(String key) {
        String base = key.trim();
        return (base.length() > 57 ? base.substring(0, 57) : base) + "-cash";
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private DevolucionVentaDTO map(DevolucionVenta d) {
        return new DevolucionVentaDTO(d.getIdDevolucionVenta(), d.getVenta().getIdVenta(), d.getFecha(),
                d.getTipo().name(), d.getEstado().name(), d.getMotivo(),
                d.getMetodoReembolso() == null ? null : d.getMetodoReembolso().name(), d.getReferenciaReembolso(),
                d.getSubtotal(), d.getIgv(), d.getTotal(), d.getReembolsos().stream()
                .map(r -> new DevolucionVentaDTO.Reembolso(r.getMetodoPago().name(), r.getMonto(), r.getReferencia()))
                .toList(), d.getDetalles().stream().map(item -> new DevolucionVentaDTO.Item(
                        item.getDetalleVenta().getIdDetalleVenta(), item.getLote().getProducto().getIdProducto(),
                        item.getLote().getProducto().getNombre(), item.getLote().getIdLote(),
                        item.getLote().getCodigoLote(), item.getCantidad(), item.getSubtotal())).toList());
    }

    private record Linea(DetalleVenta detalle, int cantidad, BigDecimal subtotal, BigDecimal base, BigDecimal igv) {}
    private record ReembolsoNormalizado(MetodoPago metodo, BigDecimal monto, String referencia) {}
}
