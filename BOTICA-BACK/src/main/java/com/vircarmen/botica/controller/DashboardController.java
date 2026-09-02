package com.vircarmen.botica.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vircarmen.botica.dto.BajoStockDTO;
import com.vircarmen.botica.dto.LoteVencerDTO;
import com.vircarmen.botica.dto.MetricasDTO;
import com.vircarmen.botica.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/metricas-dia")
    public ResponseEntity<MetricasDTO> obtenerMetricas() {
        return ResponseEntity.ok(dashboardService.obtenerMetricasHoy());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/ventas-semana")
    public ResponseEntity<List<BigDecimal>> obtenerVentasSemana() {
        // Mock rápido para ventas de la semana
        return ResponseEntity.ok(List.of(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, dashboardService.obtenerMetricasHoy().totalVentas()
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/bajo-stock")
    public ResponseEntity<List<BajoStockDTO>> obtenerBajoStock() {
        return ResponseEntity.ok(dashboardService.obtenerBajoStock());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @GetMapping("/lotes-vencer")
    public ResponseEntity<List<LoteVencerDTO>> obtenerLotesPorVencer() {
        return ResponseEntity.ok(dashboardService.obtenerLotesPorVencer());
    }
}
