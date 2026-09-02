package com.vircarmen.botica.dto;

import java.math.BigDecimal;

public class PagoRequest {
    private String metodoPago;
    private BigDecimal monto;

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
