package com.vircarmen.botica.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PagoRequest {
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    @NotNull(message = "El monto recibido es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto recibido debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El monto recibido admite máximo dos decimales")
    private BigDecimal montoRecibido;

    @Size(max = 100, message = "La referencia no puede superar 100 caracteres")
    private String referencia;

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public BigDecimal getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(BigDecimal montoRecibido) { this.montoRecibido = montoRecibido; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
