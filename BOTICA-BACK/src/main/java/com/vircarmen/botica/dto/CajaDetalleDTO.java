package com.vircarmen.botica.dto;

import java.util.List;

public record CajaDetalleDTO(
        CajaSesionDTO caja,
        ArqueoCajaDTO arqueo,
        List<MovimientoCajaDTO> movimientos
) {}
