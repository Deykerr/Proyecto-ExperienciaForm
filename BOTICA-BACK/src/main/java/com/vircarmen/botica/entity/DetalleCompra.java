package com.vircarmen.botica.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compras")
public class DetalleCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetalleCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "costo_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal costoUnitario;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    // Getters and Setters
    public Integer getIdDetalleCompra() { return idDetalleCompra; }
    public void setIdDetalleCompra(Integer idDetalleCompra) { this.idDetalleCompra = idDetalleCompra; }
    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
