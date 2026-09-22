package com.vircarmen.botica.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CajaSesionDTO(
    Integer idCajaSesion,
    String usuario,
    LocalDateTime fechaApertura,
    LocalDateTime fechaCierre,
    BigDecimal montoInicial,
    BigDecimal montoFinal,
    BigDecimal totalIngresos,
    BigDecimal totalEgresos,
    BigDecimal saldoCalculado,
    BigDecimal ventasEfectivo,
    BigDecimal ventasYape,
    BigDecimal ventasPlin,
    BigDecimal ventasTarjeta,
    BigDecimal diferencia,
    Boolean requiereRevision,
    String estado
) {}
