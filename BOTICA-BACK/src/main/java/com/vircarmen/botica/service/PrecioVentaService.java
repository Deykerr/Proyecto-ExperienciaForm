package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.exception.BusinessException;

@Service
public class PrecioVentaService {
    private final BigDecimal tasaIgv;

    public PrecioVentaService(@Value("${app.igv.tasa:0.18}") BigDecimal tasaIgv) {
        this.tasaIgv = tasaIgv;
    }

    public CalculoPrecio calcular(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a cero.");
        }
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El producto " + producto.getNombre() + " no tiene un precio de venta válido.");
        }

        int unidadesPorPresentacion = producto.getUnidadesPorPresentacion() == null
                ? 1
                : producto.getUnidadesPorPresentacion();
        BigDecimal total;

        if (unidadesPorPresentacion > 1 && producto.getPrecioPresentacion() != null) {
            if (producto.getPrecioPresentacion().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("El precio por presentación debe ser mayor a cero.");
            }
            int presentaciones = cantidad / unidadesPorPresentacion;
            int unidadesSueltas = cantidad % unidadesPorPresentacion;
            total = producto.getPrecioPresentacion().multiply(BigDecimal.valueOf(presentaciones))
                    .add(producto.getPrecioVenta().multiply(BigDecimal.valueOf(unidadesSueltas)));
        } else {
            total = producto.getPrecioVenta().multiply(BigDecimal.valueOf(cantidad));
        }

        total = total.setScale(2, RoundingMode.HALF_UP);
        String afectacion = normalizarAfectacion(producto.getTipoAfectacionIgv());
        BigDecimal baseImponible;
        BigDecimal igv;

        if ("10".equals(afectacion)) {
            baseImponible = total.divide(BigDecimal.ONE.add(tasaIgv), 2, RoundingMode.HALF_UP);
            igv = total.subtract(baseImponible);
        } else {
            baseImponible = total;
            igv = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal precioUnitarioPromedio = total.divide(
                BigDecimal.valueOf(cantidad),
                4,
                RoundingMode.HALF_UP);

        return new CalculoPrecio(
                total,
                baseImponible,
                igv,
                precioUnitarioPromedio,
                afectacion);
    }

    private String normalizarAfectacion(String valor) {
        if (valor == null || valor.isBlank()) {
            return "10";
        }
        String codigo = valor.trim();
        if (codigo.length() != 2 || !codigo.chars().allMatch(Character::isDigit)) {
            throw new BusinessException("El tipo de afectación IGV debe ser un código SUNAT de dos dígitos.");
        }
        return codigo;
    }

    public record CalculoPrecio(
            BigDecimal total,
            BigDecimal baseImponible,
            BigDecimal igv,
            BigDecimal precioUnitarioPromedio,
            String tipoAfectacionIgv
    ) {}
}
