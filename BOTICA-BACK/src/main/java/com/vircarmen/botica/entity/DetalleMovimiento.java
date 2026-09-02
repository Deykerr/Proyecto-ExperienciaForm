package com.vircarmen.botica.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_movimientos")
public class DetalleMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Movimiento movimiento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Lote lote;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }
    public Movimiento getMovimiento() { return movimiento; }
    public void setMovimiento(Movimiento movimiento) { this.movimiento = movimiento; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
