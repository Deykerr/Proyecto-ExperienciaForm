package com.vircarmen.botica.dto;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecetaRequest(
        @NotBlank @Size(max = 60) String numero,
        Integer idCliente,
        @NotBlank @Size(max = 200) String pacienteNombre,
        @Size(max = 20) String pacienteDocumento,
        @NotBlank @Size(max = 200) String medicoNombre,
        @NotBlank @Size(max = 30) String medicoColegiatura,
        @NotNull LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        @NotBlank String condicionVenta,
        @Size(max = 500) String observaciones,
        @NotEmpty @Valid List<Item> detalles) {
    public record Item(
            @NotNull Integer idProducto,
            @NotNull @Positive Integer cantidadAutorizada,
            @Size(max = 300) String indicaciones) {}
}
