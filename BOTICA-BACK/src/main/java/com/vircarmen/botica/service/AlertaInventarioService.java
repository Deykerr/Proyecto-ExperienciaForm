package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.AlertaInventarioDTO;
import com.vircarmen.botica.dto.ResumenAlertasInventarioDTO;
import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.LoteRepository;
import com.vircarmen.botica.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaInventarioService {

    private static final int DIAS_MINIMOS = 1;
    private static final int DIAS_MAXIMOS = 365;

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public ResumenAlertasInventarioDTO obtenerResumen(int diasVencimiento) {
        validarDias(diasVencimiento);

        LocalDate hoy = LocalDate.now();
        List<AlertaInventarioDTO> alertas = new ArrayList<>();

        productoRepository.findProductosConStockBajo().stream()
                .map(this::crearAlertaStock)
                .forEach(alertas::add);

        loteRepository.findLotesConStockHastaFecha(hoy.plusDays(diasVencimiento)).stream()
                .map(lote -> crearAlertaLote(lote, hoy))
                .forEach(alertas::add);

        alertas.sort(Comparator
                .comparingInt((AlertaInventarioDTO alerta) -> prioridadSeveridad(alerta.severidad()))
                .thenComparingInt(alerta -> prioridadTipo(alerta.tipo()))
                .thenComparing(AlertaInventarioDTO::producto, String.CASE_INSENSITIVE_ORDER));

        int criticas = contar(alertas, "CRITICA", AlertaInventarioDTO::severidad);
        int agotados = contar(alertas, "AGOTADO", AlertaInventarioDTO::tipo);
        int bajoStock = contar(alertas, "STOCK_BAJO", AlertaInventarioDTO::tipo);
        int vencidos = contar(alertas, "LOTE_VENCIDO", AlertaInventarioDTO::tipo);
        int proximos = contar(alertas, "LOTE_POR_VENCER", AlertaInventarioDTO::tipo);

        return new ResumenAlertasInventarioDTO(
                alertas.size(),
                criticas,
                alertas.size() - criticas,
                agotados,
                bajoStock,
                vencidos,
                proximos,
                diasVencimiento,
                LocalDateTime.now(),
                List.copyOf(alertas));
    }

    private AlertaInventarioDTO crearAlertaStock(Producto producto) {
        int stockActual = valorSeguro(producto.getStockActual());
        int stockMinimo = valorSeguro(producto.getStockMinimo());
        boolean agotado = stockActual <= 0;
        boolean critico = agotado || stockActual <= Math.max(1, stockMinimo / 2);
        int objetivoReposicion = Math.max(1, stockMinimo * 2);
        int cantidadSugerida = Math.max(1, objetivoReposicion - stockActual);

        String tipo = agotado ? "AGOTADO" : "STOCK_BAJO";
        String mensaje = agotado
                ? "El producto está agotado y no puede atender nuevas ventas."
                : "El stock actual alcanzó el mínimo configurado para reposición.";

        return new AlertaInventarioDTO(
                "PRODUCTO-" + producto.getIdProducto(),
                tipo,
                critico ? "CRITICA" : "ADVERTENCIA",
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getCodigoBarras(),
                stockActual,
                stockMinimo,
                cantidadSugerida,
                null,
                null,
                null,
                null,
                mensaje,
                "Revisar existencias y preparar una reposición de " + cantidadSugerida + " unidades." );
    }

    private AlertaInventarioDTO crearAlertaLote(Lote lote, LocalDate hoy) {
        long diasParaVencer = ChronoUnit.DAYS.between(hoy, lote.getFechaVencimiento());
        boolean vencido = diasParaVencer < 0;
        boolean critico = vencido || diasParaVencer <= 7;

        String tipo = vencido ? "LOTE_VENCIDO" : "LOTE_POR_VENCER";
        String mensaje;
        String accion;
        if (vencido) {
            mensaje = "El lote venció y todavía registra " + lote.getStockActual() + " unidades.";
            accion = "Bloquear su venta, retirarlo del área dispensable y regularizar el inventario.";
        } else if (diasParaVencer == 0) {
            mensaje = "El lote vence hoy y registra " + lote.getStockActual() + " unidades.";
            accion = "Revisar de inmediato y evitar su dispensación después del vencimiento.";
        } else {
            mensaje = "El lote vence en " + diasParaVencer + " días y registra " + lote.getStockActual() + " unidades.";
            accion = "Aplicar FEFO y evaluar devolución al proveedor o rotación controlada.";
        }

        Producto producto = lote.getProducto();
        return new AlertaInventarioDTO(
                "LOTE-" + lote.getIdLote(),
                tipo,
                critico ? "CRITICA" : "ADVERTENCIA",
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getCodigoBarras(),
                valorSeguro(producto.getStockActual()),
                valorSeguro(producto.getStockMinimo()),
                null,
                lote.getIdLote(),
                lote.getCodigoLote(),
                lote.getFechaVencimiento(),
                diasParaVencer,
                mensaje,
                accion);
    }

    private void validarDias(int diasVencimiento) {
        if (diasVencimiento < DIAS_MINIMOS || diasVencimiento > DIAS_MAXIMOS) {
            throw new BusinessException("El rango de vencimiento debe estar entre 1 y 365 días.");
        }
    }

    private int valorSeguro(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private int contar(List<AlertaInventarioDTO> alertas, String valor,
                       java.util.function.Function<AlertaInventarioDTO, String> campo) {
        return (int) alertas.stream().filter(alerta -> valor.equals(campo.apply(alerta))).count();
    }

    private int prioridadSeveridad(String severidad) {
        return "CRITICA".equals(severidad) ? 0 : 1;
    }

    private int prioridadTipo(String tipo) {
        return switch (tipo) {
            case "LOTE_VENCIDO" -> 0;
            case "AGOTADO" -> 1;
            case "LOTE_POR_VENCER" -> 2;
            default -> 3;
        };
    }
}
