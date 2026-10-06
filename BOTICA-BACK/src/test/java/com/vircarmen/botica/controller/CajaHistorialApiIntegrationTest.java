package com.vircarmen.botica.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.vircarmen.botica.entity.CajaSesion;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.repository.CajaSesionRepository;
import com.vircarmen.botica.repository.UsuarioRepository;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:botica_caja_historial;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class CajaHistorialApiIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CajaSesionRepository cajaSesionRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario admin;
    private Usuario cajero;
    private CajaSesion cajaAdmin;
    private CajaSesion cajaCajero;

    @BeforeEach
    void prepararCajas() {
        cajaSesionRepository.deleteAll();
        admin = usuarioRepository.findByUsername("admin").orElseThrow();
        cajero = usuarioRepository.findByUsername("cajero").orElseThrow();
        cajaAdmin = crearCajaCerrada(admin, LocalDateTime.now().minusDays(2), new BigDecimal("100.00"));
        cajaCajero = crearCajaCerrada(cajero, LocalDateTime.now().minusDays(1), new BigDecimal("150.00"));
    }

    @Test
    void cajeroSoloDeberiaVerSusPropiasCajas() throws Exception {
        mockMvc.perform(get("/api/caja/historial").with(user(cajero)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idCajaSesion").value(cajaCajero.getIdCajaSesion()))
                .andExpect(jsonPath("$[0].usuario").value("cajero"));
    }

    @Test
    void administradorDeberiaFiltrarHistorialPorCajeroYEstado() throws Exception {
        mockMvc.perform(get("/api/caja/historial")
                        .param("usuario", "caje")
                        .param("estado", "CERRADA")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idCajaSesion").value(cajaCajero.getIdCajaSesion()))
                .andExpect(jsonPath("$[0].montoEsperado").value(150.00));
    }

    @Test
    void administradorDeberiaListarTodasLasCajasConFiltroDeUsuarioVacio() throws Exception {
        mockMvc.perform(get("/api/caja/historial")
                        .param("usuario", "")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void cajeroNoDeberiaConsultarDetalleDeOtraCaja() throws Exception {
        mockMvc.perform(get("/api/caja/historial/{id}", cajaAdmin.getIdCajaSesion())
                        .with(user(cajero)))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorDeberiaVerElDetalleDeCualquierCaja() throws Exception {
        mockMvc.perform(get("/api/caja/historial/{id}", cajaCajero.getIdCajaSesion())
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caja.usuario").value("cajero"))
                .andExpect(jsonPath("$.caja.estado").value("CERRADA"))
                .andExpect(jsonPath("$.movimientos").isArray());
    }

    private CajaSesion crearCajaCerrada(Usuario usuario, LocalDateTime apertura, BigDecimal monto) {
        CajaSesion caja = new CajaSesion();
        caja.setUsuario(usuario);
        caja.setFechaApertura(apertura);
        caja.setFechaCierre(apertura.plusHours(8));
        caja.setMontoInicial(monto);
        caja.setMontoEsperado(monto);
        caja.setMontoFinal(monto);
        caja.setDiferencia(BigDecimal.ZERO.setScale(2));
        caja.setRequiereRevision(false);
        caja.setEstado(CajaSesion.EstadoCaja.CERRADA);
        return cajaSesionRepository.save(caja);
    }
}
