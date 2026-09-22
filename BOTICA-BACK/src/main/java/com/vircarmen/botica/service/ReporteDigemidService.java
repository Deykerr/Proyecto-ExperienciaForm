package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.ReporteDigemidDTO;
import com.vircarmen.botica.dto.ResultadoReporteDigemidRequest;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.EstadoReporteDigemid;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.ReportePrecioDigemid;
import com.vircarmen.botica.entity.ReportePrecioDigemidDetalle;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.ReportePrecioDigemidRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteDigemidService {
    private final ReportePrecioDigemidRepository reporteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ReporteDigemidDTO generar(String periodoTexto) {
        YearMonth periodo = parsePeriodo(periodoTexto);
        if (periodo.isAfter(YearMonth.now())) {
            throw new BusinessException("No se puede generar un reporte para un periodo futuro.");
        }
        ReportePrecioDigemid existente = reporteRepository.findByPeriodo(periodo.toString()).orElse(null);
        if (existente != null) return map(existente);

        List<Producto> productos = productoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoGeneral.A)
                .filter(p -> p.getRegistroSanitario() != null && !p.getRegistroSanitario().isBlank())
                .sorted(Comparator.comparing(Producto::getRegistroSanitario).thenComparing(Producto::getNombre))
                .toList();
        if (productos.isEmpty()) {
            throw new BusinessException("No hay productos activos con registro sanitario para reportar a DIGEMID.");
        }
        for (Producto producto : productos) {
            if (producto.getPrecioVenta() == null || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("El producto " + producto.getNombre() + " no tiene precio público válido.");
            }
        }

        LocalDate fechaVigencia = periodo.equals(YearMonth.now()) ? LocalDate.now() : periodo.atEndOfMonth();
        String csv = crearCsv(periodo, fechaVigencia, productos);
        Usuario usuario = usuarioRepository.findById(SecurityUtils.getUsuarioAutenticadoId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
        ReportePrecioDigemid reporte = new ReportePrecioDigemid();
        reporte.setPeriodo(periodo.toString());
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setUsuario(usuario);
        reporte.setEstado(EstadoReporteDigemid.VALIDADO);
        reporte.setCantidadProductos(productos.size());
        reporte.setArchivoCsv(csv);
        reporte.setHashArchivo(sha256(csv));
        for (Producto producto : productos) {
            ReportePrecioDigemidDetalle detalle = new ReportePrecioDigemidDetalle();
            detalle.setReporte(reporte);
            detalle.setProducto(producto);
            detalle.setRegistroSanitario(producto.getRegistroSanitario().trim().toUpperCase(Locale.ROOT));
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPresentacion(producto.getPresentacion());
            detalle.setPrecioVentaPublico(producto.getPrecioVenta().setScale(2, RoundingMode.HALF_UP));
            detalle.setStockDisponible(Math.max(0, producto.getStockActual()));
            detalle.setFechaVigencia(fechaVigencia);
            reporte.getDetalles().add(detalle);
        }
        return map(reporteRepository.save(reporte));
    }

    @Transactional
    public ReporteDigemidDTO registrarResultado(Integer id, ResultadoReporteDigemidRequest request) {
        ReportePrecioDigemid reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reporte DIGEMID no encontrado."));
        EstadoReporteDigemid estado;
        try {
            estado = EstadoReporteDigemid.valueOf(request.estado().trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException("Estado de reporte no válido.");
        }
        if (estado != EstadoReporteDigemid.ENVIADO && estado != EstadoReporteDigemid.ACEPTADO
                && estado != EstadoReporteDigemid.OBSERVADO) {
            throw new BusinessException("Solo puede registrarse un resultado ENVIADO, ACEPTADO u OBSERVADO.");
        }
        if ((estado == EstadoReporteDigemid.ENVIADO || estado == EstadoReporteDigemid.ACEPTADO)
                && (request.constancia() == null || request.constancia().isBlank())) {
            throw new BusinessException("La constancia del portal SNIPPF es obligatoria.");
        }
        if (estado == EstadoReporteDigemid.OBSERVADO
                && (request.observaciones() == null || request.observaciones().isBlank())) {
            throw new BusinessException("Debe registrar la observación informada por DIGEMID.");
        }
        reporte.setEstado(estado);
        reporte.setFechaEnvio(LocalDateTime.now());
        reporte.setConstancia(normalizar(request.constancia()));
        reporte.setObservaciones(normalizar(request.observaciones()));
        return map(reporteRepository.save(reporte));
    }

    @Transactional(readOnly = true)
    public List<ReporteDigemidDTO> listar() {
        return reporteRepository.findAllByOrderByPeriodoDesc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public byte[] descargar(Integer id) {
        ReportePrecioDigemid reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reporte DIGEMID no encontrado."));
        // BOM UTF-8 para conservar tildes al abrir el CSV en Excel.
        return ("\uFEFF" + reporte.getArchivoCsv()).getBytes(StandardCharsets.UTF_8);
    }

    private String crearCsv(YearMonth periodo, LocalDate fecha, List<Producto> productos) {
        StringBuilder csv = new StringBuilder("PERIODO;REGISTRO_SANITARIO;NOMBRE_PRODUCTO;PRESENTACION;"
                + "PRECIO_VENTA_PUBLICO_INCLUYE_IGV;STOCK_DISPONIBLE;FECHA_VIGENCIA\r\n");
        for (Producto p : productos) {
            csv.append(periodo).append(';').append(campo(p.getRegistroSanitario())).append(';')
                    .append(campo(p.getNombre())).append(';').append(campo(p.getPresentacion())).append(';')
                    .append(p.getPrecioVenta().setScale(2, RoundingMode.HALF_UP).toPlainString()).append(';')
                    .append(Math.max(0, p.getStockActual())).append(';').append(fecha).append("\r\n");
        }
        return csv.toString();
    }

    private String campo(String valor) {
        if (valor == null) return "";
        String limpio = valor.replace("\r", " ").replace("\n", " ").replace("\"", "\"\"");
        return '"' + limpio + '"';
    }

    private YearMonth parsePeriodo(String valor) {
        try {
            return YearMonth.parse(valor);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new BusinessException("El periodo debe tener formato AAAA-MM.");
        }
    }

    private String sha256(String contenido) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(contenido.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo calcular el hash del reporte.", ex);
        }
    }

    private String normalizar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }

    private ReporteDigemidDTO map(ReportePrecioDigemid r) {
        return new ReporteDigemidDTO(r.getIdReporte(), r.getPeriodo(), r.getFechaGeneracion(),
                r.getEstado().name(), r.getCantidadProductos(), r.getHashArchivo(), r.getFechaEnvio(),
                r.getConstancia(), r.getObservaciones());
    }
}
