package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.DetalleVentaDTO;
import com.vircarmen.botica.dto.VentaRequest;
import com.vircarmen.botica.dto.VentaResumenDTO;
import com.vircarmen.botica.entity.CajaSesion;
import com.vircarmen.botica.entity.Cliente;
import com.vircarmen.botica.entity.Comprobante;
import com.vircarmen.botica.entity.DetalleVenta;
import com.vircarmen.botica.entity.EstadoVenta;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.Receta;
import com.vircarmen.botica.entity.TipoComprobante;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.entity.Venta;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.exception.CajaNoAbiertaException;
import com.vircarmen.botica.repository.CajaSesionRepository;
import com.vircarmen.botica.repository.ClienteRepository;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.repository.VentaRepository;
import com.vircarmen.botica.repository.DetalleVentaRepository;
import com.vircarmen.botica.security.SecurityUtils;
import com.vircarmen.botica.service.PrecioVentaService.CalculoPrecio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaService {
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CajaSesionRepository cajaRepository;
    private final InventarioService inventarioService;
    private final PrecioVentaService precioVentaService;
    private final PagoService pagoService;
    private final ComprobanteService comprobanteService;
    private final RecetaService recetaService;
    private final DocumentoElectronicoService documentoElectronicoService;

    @Transactional(readOnly = true)
    public List<VentaResumenDTO> obtenerTodasLasVentas() {
        return ventaRepository.findAll().stream()
                .sorted(Comparator.comparing(Venta::getFechaEmision).reversed())
                .map(this::mapResumen).toList();
    }

    @Transactional
    public VentaResumenDTO registrarVenta(VentaRequest request) {
        String idempotencyKey = request.getIdempotencyKey().trim();
        Venta existente = ventaRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existente != null) {
            return mapResumen(existente);
        }

        TipoComprobante tipoComprobante = parseTipoComprobante(request.getTipoComprobante());
        Cliente cliente = obtenerCliente(request.getIdCliente());
        validarProductosSinDuplicados(request.getItems());

        Integer idUsuario = SecurityUtils.getUsuarioAutenticadoId();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
        CajaSesion caja = cajaRepository
                .findByUsuarioIdUsuarioAndEstado(idUsuario, CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new CajaNoAbiertaException("El usuario no tiene una caja abierta."));

        List<LineaVenta> lineas = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal igv = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, Integer> cantidadesReceta = new HashMap<>();

        List<DetalleVentaDTO> itemsOrdenados = request.getItems().stream()
                .sorted(Comparator.comparing(DetalleVentaDTO::idProducto))
                .toList();
        for (DetalleVentaDTO item : itemsOrdenados) {
            Producto producto = productoRepository.findByIdWithLock(item.idProducto())
                    .orElseThrow(() -> new BusinessException("Producto no encontrado: " + item.idProducto()));
            CalculoPrecio calculo = precioVentaService.calcular(producto, item.cantidad());
            lineas.add(new LineaVenta(producto, item.cantidad(), calculo));
            subtotal = subtotal.add(calculo.baseImponible());
            igv = igv.add(calculo.igv());
            total = total.add(calculo.total());
            if (producto.getCondicionVenta().requiereReceta()) {
                cantidadesReceta.put(producto.getIdProducto(), item.cantidad());
            }
        }

        validarClienteParaComprobante(tipoComprobante, cliente, total);

        Receta receta = recetaService.validarYDispensar(request.getIdReceta(), cliente, cantidadesReceta);

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setCajaSesion(caja);
        venta.setFechaEmision(LocalDateTime.now());
        venta.setEstado(EstadoVenta.PENDIENTE);
        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);
        venta.setIdempotencyKey(idempotencyKey);
        venta.setReceta(receta);
        venta.setReferenciaReceta(receta != null ? receta.getNumero()
                : isBlank(request.getReferenciaReceta()) ? null : request.getReferenciaReceta().trim());
        venta = ventaRepository.saveAndFlush(venta);

        for (LineaVenta linea : lineas) {
            List<DetalleVenta> detalles = inventarioService.consumirStockFEFO(
                    linea.producto(),
                    linea.cantidad(),
                    venta,
                    linea.calculo(),
                    usuario);
            venta.getDetalles().addAll(detalles);
        }

        pagoService.procesarPagos(venta, request.getPagos(), caja);
        Comprobante comprobante = comprobanteService.generarComprobante(venta, tipoComprobante);
        venta.setComprobante(comprobante);
        venta.setEstado(EstadoVenta.PAGADA);
        Venta guardada = ventaRepository.save(venta);
        documentoElectronicoService.encolarComprobante(comprobante);
        return mapResumen(guardada);
    }

    private Cliente obtenerCliente(Integer idCliente) {
        if (idCliente == null) {
            return null;
        }
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado."));
    }

    private void validarClienteParaComprobante(TipoComprobante tipo, Cliente cliente, BigDecimal total) {
        if (tipo == TipoComprobante.FACTURA
                && (cliente == null || !"RUC".equalsIgnoreCase(cliente.getTipoDocumento())
                    || cliente.getNumeroDocumento() == null
                    || !cliente.getNumeroDocumento().matches("\\d{11}"))) {
            throw new BusinessException("La factura requiere un cliente identificado con RUC.");
        }
        if (tipo == TipoComprobante.BOLETA && total.compareTo(new BigDecimal("700.00")) > 0
                && (cliente == null || cliente.getNumeroDocumento() == null
                    || cliente.getNumeroDocumento().isBlank())) {
            throw new BusinessException("Las boletas mayores a S/ 700 requieren identificar al cliente.");
        }
    }

    private void validarProductosSinDuplicados(List<DetalleVentaDTO> items) {
        Set<Integer> ids = new HashSet<>();
        for (DetalleVentaDTO item : items) {
            if (!ids.add(item.idProducto())) {
                throw new BusinessException("Un producto no puede aparecer dos veces en la misma venta.");
            }
        }
    }

    private TipoComprobante parseTipoComprobante(String valor) {
        try {
            return TipoComprobante.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException("Tipo de comprobante no válido: " + valor);
        }
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    private VentaResumenDTO mapResumen(Venta venta) {
        Comprobante c = venta.getComprobante();
        return new VentaResumenDTO(venta.getIdVenta(), venta.getFechaEmision(),
                venta.getCliente() == null ? "CLIENTES VARIOS" : venta.getCliente().getNombreRazonSocial(),
                venta.getEstado().name(), venta.getSubtotal(), venta.getIgv(), venta.getTotal(),
                c == null ? null : c.getTipoComprobante().name(),
                c == null ? null : c.getSerie() + "-" + c.getCorrelativo(),
                venta.getReceta() == null ? null : venta.getReceta().getIdReceta(),
                venta.getReceta() == null ? null : venta.getReceta().getNumero(),
                venta.getDetalles().stream().map(d -> {
                    int devuelta = Math.toIntExact(detalleVentaRepository.sumCantidadDevuelta(d.getIdDetalleVenta()));
                    return new VentaResumenDTO.Item(d.getIdDetalleVenta(), d.getLote().getProducto().getIdProducto(),
                            d.getLote().getProducto().getNombre(), d.getLote().getIdLote(), d.getLote().getCodigoLote(),
                            d.getCantidad(), devuelta, d.getCantidad() - devuelta, d.getSubtotal());
                }).toList());
    }

    private record LineaVenta(Producto producto, int cantidad, CalculoPrecio calculo) {}
}
