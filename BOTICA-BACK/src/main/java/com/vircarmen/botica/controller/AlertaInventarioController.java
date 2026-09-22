package com.vircarmen.botica.controller;

import com.vircarmen.botica.dto.ResumenAlertasInventarioDTO;
import com.vircarmen.botica.service.AlertaInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alertas/inventario")
@RequiredArgsConstructor
public class AlertaInventarioController {

    private final AlertaInventarioService alertaInventarioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<ResumenAlertasInventarioDTO> obtenerAlertas(
            @RequestParam(defaultValue = "30") int diasVencimiento) {
        return ResponseEntity.ok(alertaInventarioService.obtenerResumen(diasVencimiento));
    }
}
