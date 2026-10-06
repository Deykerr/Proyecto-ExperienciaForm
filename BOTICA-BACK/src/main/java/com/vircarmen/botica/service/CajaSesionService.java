package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.ArqueoCajaDTO;
import com.vircarmen.botica.dto.ArqueoAprobacionRequest;
import com.vircarmen.botica.dto.ArqueoDenominacionDTO;
import com.vircarmen.botica.dto.CajaDetalleDTO;
import com.vircarmen.botica.dto.CajaSesionCierreRequest;
import com.vircarmen.botica.dto.CajaSesionDTO;
import com.vircarmen.botica.dto.CajaSesionRequest;
import com.vircarmen.botica.dto.MovimientoCajaDTO;
import com.vircarmen.botica.dto.MovimientoCajaRequest;
import com.vircarmen.botica.entity.ArqueoCaja;
import com.vircarmen.botica.entity.ArqueoDenominacion;
import com.vircarmen.botica.entity.CajaSesion;
import com.vircarmen.botica.entity.EstadoArqueo;
import com.vircarmen.botica.entity.EstadoPago;
import com.vircarmen.botica.entity.MetodoPago;
import com.vircarmen.botica.entity.MovimientoCaja;
import com.vircarmen.botica.entity.Rol;
import com.vircarmen.botica.entity.TipoMovimientoCaja;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.ArqueoCajaRepository;
import com.vircarmen.botica.repository.CajaSesionRepository;
import com.vircarmen.botica.repository.MovimientoCajaRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CajaSesionService {
    private static final List<BigDecimal> DENOMINACIONES_PEN = List.of(
            new BigDecimal("0.10"), new BigDecimal("0.20"), new BigDecimal("0.50"),
            new BigDecimal("1.00"), new BigDecimal("2.00"), new BigDecimal("5.00"),
            new BigDecimal("10.00"), new BigDecimal("20.00"), new BigDecimal("50.00"),
            new BigDecimal("100.00"), new BigDecimal("200.00"));

    private final CajaSesionRepository cajaSesionRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final ArqueoCajaRepository arqueoCajaRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.caja.tolerancia-diferencia:0.00}")
    private BigDecimal toleranciaDiferencia;

    @Transactional
    public CajaSesionDTO abrirCaja(CajaSesionRequest request) {
        Integer idUsuario = SecurityUtils.getUsuarioAutenticadoId();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
        if (cajaSesionRepository.findByUsuarioIdUsuarioAndEstado(idUsuario, CajaSesion.EstadoCaja.ABIERTA).isPresent()) {
            throw new BusinessException("El usuario ya tiene una caja abierta.");
        }
        CajaSesion caja = new CajaSesion();
        caja.setUsuario(usuario);
        caja.setFechaApertura(LocalDateTime.now());
        caja.setMontoInicial(request.montoInicial().setScale(2, RoundingMode.HALF_UP));
        caja.setEstado(CajaSesion.EstadoCaja.ABIERTA);
        return mapToDTO(cajaSesionRepository.save(caja));
    }

    @Transactional
    public CajaSesionDTO cerrarCaja(Integer idCaja, CajaSesionCierreRequest request) {
        Integer idUsuario = SecurityUtils.getUsuarioAutenticadoId();
        CajaSesion caja = cajaSesionRepository.findByIdWithLock(idCaja)
                .orElseThrow(() -> new BusinessException("Caja no encontrada."));
        if (!caja.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new BusinessException("No tienes permiso para cerrar una caja de otro usuario.");
        }
        if (caja.getEstado() == CajaSesion.EstadoCaja.CERRADA) {
            throw new BusinessException("Esta caja ya se encuentra cerrada.");
        }
        if (arqueoCajaRepository.findByCajaSesionIdCajaSesion(idCaja).isPresent()) {
            throw new BusinessException("La caja ya tiene un arqueo registrado.");
        }

        BigDecimal contadoDenominaciones = calcularDenominaciones(request.denominaciones());
        BigDecimal contado = contadoDenominaciones != null ? contadoDenominaciones
                : request.montoFinal() == null ? null : request.montoFinal().setScale(2, RoundingMode.HALF_UP);
        if (contado == null) {
            throw new BusinessException("Debe registrar el conteo por denominaciones o el monto final.");
        }
        if (contadoDenominaciones != null && request.montoFinal() != null
                && contadoDenominaciones.compareTo(request.montoFinal().setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new BusinessException("El monto final no coincide con el conteo por denominaciones.");
        }

        Resumen resumen = calcularResumen(caja);
        BigDecimal diferencia = contado.subtract(resumen.saldoEsperado()).setScale(2, RoundingMode.HALF_UP);
        boolean requiereRevision = diferencia.abs().compareTo(toleranciaDiferencia) > 0;
        EstadoArqueo estado = diferencia.signum() == 0 ? EstadoArqueo.CUADRADO
                : diferencia.signum() > 0 ? EstadoArqueo.SOBRANTE : EstadoArqueo.FALTANTE;

        ArqueoCaja arqueo = new ArqueoCaja();
        arqueo.setCajaSesion(caja);
        arqueo.setTotalContado(contado);
        arqueo.setSaldoEsperado(resumen.saldoEsperado());
        arqueo.setDiferencia(diferencia);
        arqueo.setEstado(estado);
        arqueo.setObservaciones(normalizar(request.observaciones()));
        arqueo.setUsuario(caja.getUsuario());
        arqueo.setFecha(LocalDateTime.now());
        if (request.denominaciones() != null) {
            for (CajaSesionCierreRequest.Denominacion item : request.denominaciones()) {
                if (item.cantidad() == null || item.cantidad() == 0) continue;
                ArqueoDenominacion detalle = new ArqueoDenominacion();
                detalle.setArqueo(arqueo);
                detalle.setDenominacion(item.denominacion().setScale(2, RoundingMode.HALF_UP));
                detalle.setCantidad(item.cantidad());
                detalle.setSubtotal(detalle.getDenominacion().multiply(BigDecimal.valueOf(item.cantidad()))
                        .setScale(2, RoundingMode.HALF_UP));
                arqueo.getDenominaciones().add(detalle);
            }
        }
        arqueoCajaRepository.save(arqueo);

        caja.setFechaCierre(LocalDateTime.now());
        caja.setMontoFinal(contado);
        caja.setMontoEsperado(resumen.saldoEsperado());
        caja.setDiferencia(diferencia);
        caja.setObservacionesCierre(normalizar(request.observaciones()));
        caja.setRequiereRevision(requiereRevision);
        caja.setEstado(CajaSesion.EstadoCaja.CERRADA);
        return mapToDTO(cajaSesionRepository.save(caja));
    }

    @Transactional
    public CajaSesionDTO registrarMovimiento(Integer idCaja, MovimientoCajaRequest request) {
        MovimientoCaja existente = movimientoCajaRepository.findByIdempotencyKey(request.idempotencyKey().trim()).orElse(null);
        if (existente != null) return mapToDTO(existente.getCajaSesion());
        CajaSesion caja = cajaSesionRepository.findByIdWithLock(idCaja)
                .orElseThrow(() -> new BusinessException("Caja no encontrada."));
        Integer idUsuario = SecurityUtils.getUsuarioAutenticadoId();
        if (!caja.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new BusinessException("No puedes registrar movimientos en una caja de otro usuario.");
        }
        if (caja.getEstado() != CajaSesion.EstadoCaja.ABIERTA) {
            throw new BusinessException("La caja está cerrada.");
        }
        TipoMovimientoCaja tipo;
        try {
            tipo = TipoMovimientoCaja.valueOf(request.tipoMovimiento().trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException("Tipo de movimiento de caja no válido.");
        }
        BigDecimal monto = request.monto().setScale(2, RoundingMode.HALF_UP);
        if (tipo == TipoMovimientoCaja.EGRESO
                && monto.compareTo(calcularResumen(caja).saldoEsperado()) > 0) {
            throw new BusinessException("El egreso supera el efectivo disponible en caja.");
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setCajaSesion(caja);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setMonto(monto);
        movimiento.setMotivo(request.motivo().trim());
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setUsuario(usuario);
        movimiento.setReferenciaTipo("MANUAL");
        movimiento.setIdempotencyKey(request.idempotencyKey().trim());
        movimientoCajaRepository.save(movimiento);
        caja.getMovimientos().add(movimiento);
        return mapToDTO(caja);
    }

    @Transactional
    public CajaSesionDTO aprobarDiferencia(Integer idCaja, ArqueoAprobacionRequest request) {
        CajaSesion caja = cajaSesionRepository.findByIdWithLock(idCaja)
                .orElseThrow(() -> new BusinessException("Caja no encontrada."));
        if (!Boolean.TRUE.equals(caja.getRequiereRevision())) {
            throw new BusinessException("El arqueo no requiere aprobación.");
        }
        ArqueoCaja arqueo = arqueoCajaRepository.findByCajaSesionIdCajaSesion(idCaja)
                .orElseThrow(() -> new BusinessException("Arqueo no encontrado."));
        Usuario aprobador = usuarioRepository.findById(SecurityUtils.getUsuarioAutenticadoId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
        arqueo.setAprobadoPor(aprobador);
        arqueo.setFechaAprobacion(LocalDateTime.now());
        arqueo.setObservacionAprobacion(request.observacion().trim());
        arqueo.setEstado(EstadoArqueo.APROBADO);
        arqueoCajaRepository.save(arqueo);
        caja.setRequiereRevision(false);
        return mapToDTO(cajaSesionRepository.save(caja));
    }

    @Transactional(readOnly = true)
    public CajaSesionDTO obtenerCajaActual(Integer ignorado) {
        return cajaSesionRepository.findByUsuarioIdUsuarioAndEstado(
                SecurityUtils.getUsuarioAutenticadoId(), CajaSesion.EstadoCaja.ABIERTA)
                .map(this::mapToDTO).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CajaSesionDTO> listarHistorial(LocalDate desde, LocalDate hasta,
            String estado, String usuario) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        Usuario actual = usuarioActual();
        boolean esAdmin = actual.getRol() == Rol.ADMIN;
        Integer idUsuario = esAdmin ? null : actual.getIdUsuario();
        String usuarioNormalizado = normalizar(usuario);
        String usuarioFiltro = esAdmin && usuarioNormalizado != null ? usuarioNormalizado : "";
        CajaSesion.EstadoCaja estadoFiltro = parseEstado(estado);
        LocalDateTime fechaDesde = desde == null ? null : desde.atStartOfDay();
        LocalDateTime fechaHasta = hasta == null ? null : hasta.plusDays(1).atStartOfDay();

        Specification<CajaSesion> filtros = (root, query, cb) -> cb.conjunction();
        if (idUsuario != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("usuario").get("idUsuario"), idUsuario));
        }
        if (!usuarioFiltro.isEmpty()) {
            String patron = "%" + usuarioFiltro.toLowerCase(Locale.ROOT) + "%";
            filtros = filtros.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("usuario").get("username")), patron));
        }
        if (estadoFiltro != null) {
            filtros = filtros.and((root, query, cb) -> cb.equal(root.get("estado"), estadoFiltro));
        }
        if (fechaDesde != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("fechaApertura"), fechaDesde));
        }
        if (fechaHasta != null) {
            filtros = filtros.and((root, query, cb) -> cb.lessThan(root.get("fechaApertura"), fechaHasta));
        }

        return cajaSesionRepository.findAll(filtros,
                        PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "fechaApertura")))
                .getContent().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CajaDetalleDTO obtenerDetalleHistorial(Integer idCaja) {
        CajaSesion caja = cajaSesionRepository.findById(idCaja)
                .orElseThrow(() -> new NoSuchElementException("Caja no encontrada."));
        Usuario actual = usuarioActual();
        if (actual.getRol() != Rol.ADMIN
                && !caja.getUsuario().getIdUsuario().equals(actual.getIdUsuario())) {
            throw new AccessDeniedException("No puedes consultar una caja de otro usuario.");
        }

        ArqueoCajaDTO arqueo = arqueoCajaRepository.findByCajaSesionIdCajaSesion(idCaja)
                .map(this::mapArqueo)
                .orElse(null);
        List<MovimientoCajaDTO> movimientos = movimientoCajaRepository
                .findByCajaSesionIdCajaSesionOrderByFechaAsc(idCaja).stream()
                .map(this::mapMovimiento)
                .toList();
        return new CajaDetalleDTO(mapToDTO(caja), arqueo, movimientos);
    }

    private BigDecimal calcularDenominaciones(List<CajaSesionCierreRequest.Denominacion> items) {
        if (items == null || items.isEmpty()) return null;
        Set<BigDecimal> usadas = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CajaSesionCierreRequest.Denominacion item : items) {
            if (item.denominacion() == null || item.cantidad() == null) {
                throw new BusinessException("Cada denominación requiere valor y cantidad.");
            }
            BigDecimal denominacion = item.denominacion().setScale(2, RoundingMode.HALF_UP);
            boolean valida = DENOMINACIONES_PEN.stream().anyMatch(d -> d.compareTo(denominacion) == 0);
            if (!valida) throw new BusinessException("Denominación PEN no válida: " + denominacion);
            if (!usadas.add(denominacion)) throw new BusinessException("Una denominación no puede repetirse.");
            total = total.add(denominacion.multiply(BigDecimal.valueOf(item.cantidad())));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private CajaSesionDTO mapToDTO(CajaSesion caja) {
        Resumen r = calcularResumen(caja);
        return new CajaSesionDTO(caja.getIdCajaSesion(), caja.getUsuario().getUsername(), caja.getFechaApertura(),
                caja.getFechaCierre(), caja.getMontoInicial(), caja.getMontoFinal(), caja.getMontoEsperado(),
                caja.getObservacionesCierre(), r.totalIngresosCaja(),
                r.egresosCaja(), r.ingresosManuales(), r.egresosManuales(), r.reembolsosEfectivo(),
                r.saldoEsperado(), r.efectivo(), r.yape(), r.plin(), r.tarjeta(),
                caja.getDiferencia(), caja.getRequiereRevision(), caja.getEstado().name());
    }

    private MovimientoCajaDTO mapMovimiento(MovimientoCaja movimiento) {
        return new MovimientoCajaDTO(movimiento.getIdMovimientoCaja(), movimiento.getTipoMovimiento().name(),
                movimiento.getMonto(), movimiento.getMotivo(), movimiento.getFecha(),
                movimiento.getUsuario().getUsername(), movimiento.getReferenciaTipo(),
                movimiento.getReferenciaId());
    }

    private ArqueoCajaDTO mapArqueo(ArqueoCaja arqueo) {
        List<ArqueoDenominacionDTO> denominaciones = arqueo.getDenominaciones().stream()
                .sorted(Comparator.comparing(d -> d.getDenominacion(), Comparator.reverseOrder()))
                .map(d -> new ArqueoDenominacionDTO(d.getDenominacion(), d.getCantidad(), d.getSubtotal()))
                .toList();
        return new ArqueoCajaDTO(arqueo.getIdArqueo(), arqueo.getTotalContado(), arqueo.getSaldoEsperado(),
                arqueo.getDiferencia(), arqueo.getEstado().name(), arqueo.getObservaciones(), arqueo.getFecha(),
                arqueo.getAprobadoPor() == null ? null : arqueo.getAprobadoPor().getUsername(),
                arqueo.getFechaAprobacion(), arqueo.getObservacionAprobacion(), denominaciones);
    }

    private Resumen calcularResumen(CajaSesion caja) {
        BigDecimal efectivo = pagos(caja, MetodoPago.EFECTIVO);
        BigDecimal yape = pagos(caja, MetodoPago.YAPE);
        BigDecimal plin = pagos(caja, MetodoPago.PLIN);
        BigDecimal tarjeta = pagos(caja, MetodoPago.TARJETA);
        BigDecimal ingresos = caja.getMovimientos().stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimientoCaja.INGRESO)
                .map(MovimientoCaja::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal egresos = caja.getMovimientos().stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimientoCaja.EGRESO)
                .map(MovimientoCaja::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ingresosManuales = movimientos(caja, TipoMovimientoCaja.INGRESO, "MANUAL");
        BigDecimal egresosManuales = movimientos(caja, TipoMovimientoCaja.EGRESO, "MANUAL");
        BigDecimal reembolsosEfectivo = movimientos(caja, TipoMovimientoCaja.EGRESO, "DEVOLUCION_VENTA");
        BigDecimal totalIngresosCaja = efectivo.add(ingresos);
        BigDecimal saldo = caja.getMontoInicial().add(totalIngresosCaja).subtract(egresos)
                .setScale(2, RoundingMode.HALF_UP);
        return new Resumen(efectivo, yape, plin, tarjeta, totalIngresosCaja, egresos,
                ingresosManuales, egresosManuales, reembolsosEfectivo, saldo);
    }

    private BigDecimal movimientos(CajaSesion caja, TipoMovimientoCaja tipo, String referenciaTipo) {
        return caja.getMovimientos().stream()
                .filter(m -> m.getTipoMovimiento() == tipo)
                .filter(m -> referenciaTipo.equalsIgnoreCase(m.getReferenciaTipo()))
                .map(MovimientoCaja::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal pagos(CajaSesion caja, MetodoPago metodo) {
        return caja.getPagos().stream()
                .filter(p -> p.getEstado() == EstadoPago.APROBADO && p.getMetodoPago() == metodo)
                .map(p -> p.getMonto()).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private CajaSesion.EstadoCaja parseEstado(String estado) {
        if (estado == null || estado.isBlank() || "TODAS".equalsIgnoreCase(estado)) return null;
        try {
            return CajaSesion.EstadoCaja.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("El estado de caja no es válido.");
        }
    }

    private Usuario usuarioActual() {
        return usuarioRepository.findById(SecurityUtils.getUsuarioAutenticadoId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
    }

    private record Resumen(BigDecimal efectivo, BigDecimal yape, BigDecimal plin, BigDecimal tarjeta,
                           BigDecimal totalIngresosCaja, BigDecimal egresosCaja, BigDecimal ingresosManuales,
                           BigDecimal egresosManuales, BigDecimal reembolsosEfectivo,
                           BigDecimal saldoEsperado) {}
}
