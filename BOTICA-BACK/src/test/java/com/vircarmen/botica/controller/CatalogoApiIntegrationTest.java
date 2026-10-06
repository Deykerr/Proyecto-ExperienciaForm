package com.vircarmen.botica.controller;

import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:botica_catalogo_api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.flyway.enabled=false",
        "jwt.secret=dGVzdC1vbmx5LWp3dC1rZXktdGhhdC1pcy1hdC1sZWFzdC0zMi1ieXRlcw==",
        "app.demo-data.enabled=true",
        "app.demo-data.admin-password=AdminLocal2026*",
        "app.demo-data.cashier-password=CajeroLocal2026*",
        "app.demo-data.stockkeeper-password=AlmacenLocal2026*"
})
@ActiveProfiles({"test", "dev"})
@AutoConfigureMockMvc
class CatalogoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void productosDeberianResponderDtoConCategoria() throws Exception {
        mockMvc.perform(get("/api/productos")
                        .param("page", "0")
                        .param("size", "20")
                        .with(user("almacenero").roles("ALMACENERO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nombreCategoria").isNotEmpty());
    }

    @Test
    void destacadosDeberianMostrarProductosActivosConStockSinBusqueda() throws Exception {
        mockMvc.perform(get("/api/productos/destacados")
                        .param("limite", "6")
                        .with(user("cajero").roles("CAJERO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].idProducto").isNumber())
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].stockActual").isNumber())
                .andExpect(jsonPath("$[?(@.nombre == 'Loratadina 10 mg Demo')]").isEmpty());
    }

    @Test
    void lotesDeberianResponderDtoPlanoSinErrorDeSerializacion() throws Exception {
        mockMvc.perform(get("/api/lotes")
                        .with(user("almacenero").roles("ALMACENERO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idLote").isNumber())
                .andExpect(jsonPath("$[0].producto").isString())
                .andExpect(jsonPath("$[0].codigoLote").isString());
    }

    @Test
    void ventaDeberiaCompletarFlujoYResponderResumenSinEntidadesPerezosas() throws Exception {
        Producto producto = productoRepository.findByCodigoBarras("775000000001").orElseThrow();
        Usuario cajero = usuarioRepository.findByUsername("cajero").orElseThrow();
        int stockAnterior = producto.getStockActual();

        mockMvc.perform(post("/api/caja/abrir")
                        .with(user(cajero))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montoInicial\":100.00}"))
                .andExpect(status().isCreated());

        String ventaJson = """
                {
                  "tipoComprobante": "BOLETA",
                  "idempotencyKey": "integration-venta-001",
                  "items": [{"idProducto": %d, "cantidad": 1}],
                  "pagos": [{"metodoPago": "EFECTIVO", "montoRecibido": 8.50}]
                }
                """.formatted(producto.getIdProducto());

        mockMvc.perform(post("/api/ventas")
                        .with(user(cajero))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PAGADA"))
                .andExpect(jsonPath("$.numeroComprobante").isNotEmpty())
                .andExpect(jsonPath("$.detalles[0].producto").value("Paracetamol 500 mg Demo"))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(1));

        Producto actualizado = productoRepository.findByCodigoBarras("775000000001").orElseThrow();
        assertEquals(stockAnterior - 1, actualizado.getStockActual());
    }
}
