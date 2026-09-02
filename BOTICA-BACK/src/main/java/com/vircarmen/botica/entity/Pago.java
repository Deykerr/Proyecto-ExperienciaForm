package com.vircarmen.botica.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Column(length = 100)
    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_sesion_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private CajaSesion cajaSesion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.APROBADO;

    // Getters and Setters
    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public CajaSesion getCajaSesion() { return cajaSesion; }
    public void setCajaSesion(CajaSesion cajaSesion) { this.cajaSesion = cajaSesion; }
    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }
}
