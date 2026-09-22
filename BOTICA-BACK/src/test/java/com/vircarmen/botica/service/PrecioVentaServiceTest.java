package com.vircarmen.botica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.service.PrecioVentaService.CalculoPrecio;

class PrecioVentaServiceTest {

    private final PrecioVentaService service = new PrecioVentaService(new BigDecimal("0.18"));

    @Test
    void calculaPresentacionMasUnidadesSueltasEImpuestoIncluido() {
        Producto producto = producto("1.50", 12, "12.00", "10");

        CalculoPrecio calculo = service.calcular(producto, 14);

        assertEquals(new BigDecimal("15.00"), calculo.total());
        assertEquals(new BigDecimal("12.71"), calculo.baseImponible());
        assertEquals(new BigDecimal("2.29"), calculo.igv());
        assertEquals(new BigDecimal("1.0714"), calculo.precioUnitarioPromedio());
    }

    @Test
    void productoExoneradoNoGeneraIgv() {
        Producto producto = producto("2.00", 1, null, "20");

        CalculoPrecio calculo = service.calcular(producto, 3);

        assertEquals(new BigDecimal("6.00"), calculo.total());
        assertEquals(new BigDecimal("6.00"), calculo.baseImponible());
        assertEquals(new BigDecimal("0.00"), calculo.igv());
    }

    private Producto producto(String precioUnidad, int unidades, String precioPresentacion, String afectacion) {
        Producto producto = new Producto();
        producto.setNombre("Producto de prueba");
        producto.setPrecioVenta(new BigDecimal(precioUnidad));
        producto.setUnidadesPorPresentacion(unidades);
        producto.setPrecioPresentacion(precioPresentacion == null ? null : new BigDecimal(precioPresentacion));
        producto.setTipoAfectacionIgv(afectacion);
        return producto;
    }
}
