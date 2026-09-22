package com.vircarmen.botica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

public record AnulacionVentaRequest(
        @NotBlank @Size(max = 64) String idempotencyKey,
        @NotBlank @Size(max = 300) String motivo,
        String metodoReembolso,
        @Size(max = 100) String referenciaReembolso,
        @Valid java.util.List<DevolucionVentaRequest.Reembolso> reembolsos) {}
