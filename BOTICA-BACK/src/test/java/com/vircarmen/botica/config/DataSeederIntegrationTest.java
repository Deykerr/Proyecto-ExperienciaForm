package com.vircarmen.botica.config;

import com.vircarmen.botica.entity.Rol;
import com.vircarmen.botica.repository.LoteRepository;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:botica_demo_seed;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class DataSeederIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private LoteRepository loteRepository;

    @Test
    void deberiaCrearUsuariosPorRolYCasosDeInventario() {
        assertEquals(Rol.ADMIN, usuarioRepository.findByUsername("admin").orElseThrow().getRol());
        assertEquals(Rol.CAJERO, usuarioRepository.findByUsername("cajero").orElseThrow().getRol());
        assertEquals(Rol.ALMACENERO, usuarioRepository.findByUsername("almacenero").orElseThrow().getRol());

        assertTrue(productoRepository.findByCodigoBarras("775000000002").orElseThrow().getStockActual()
                <= productoRepository.findByCodigoBarras("775000000002").orElseThrow().getStockMinimo());
        assertEquals(0, productoRepository.findByCodigoBarras("775000000003").orElseThrow().getStockActual());
        assertTrue(loteRepository.findLotesConStockHastaFecha(LocalDate.now().plusDays(30)).stream()
                .anyMatch(lote -> lote.getFechaVencimiento().isBefore(LocalDate.now())));
        assertTrue(loteRepository.findLotesConStockHastaFecha(LocalDate.now().plusDays(30)).stream()
                .anyMatch(lote -> !lote.getFechaVencimiento().isBefore(LocalDate.now())));
    }
}
