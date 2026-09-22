package com.vircarmen.botica.dto;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 50) String codigoBarras,
        @Size(max = 20) String codigoSunat,
        @NotBlank @Pattern(regexp = "\\d{2}", message = "Debe ser un código SUNAT de dos dígitos") String tipoAfectacionIgv,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal precioVenta,
        @NotNull @PositiveOrZero Integer stockMinimo,
        @NotNull Integer idCategoria,
        @Positive Integer unidadesPorPresentacion,
        @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal precioPresentacion,
        Boolean requiereReceta,
        String condicionVenta,
        @Size(max = 50) String registroSanitario
) {}
