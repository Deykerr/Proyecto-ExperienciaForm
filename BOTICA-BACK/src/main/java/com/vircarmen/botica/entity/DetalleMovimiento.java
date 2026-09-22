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
    @JoinColumn(name = "lote_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Lote lote;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lotes"})
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "stock_lote_anterior")
    private Integer stockLoteAnterior;

    @Column(name = "stock_lote_posterior")
    private Integer stockLotePosterior;

    @Column(name = "stock_producto_anterior", nullable = false)
    private Integer stockProductoAnterior;

    @Column(name = "stock_producto_posterior", nullable = false)
    private Integer stockProductoPosterior;

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }
    public Movimiento getMovimiento() { return movimiento; }
    public void setMovimiento(Movimiento movimiento) { this.movimiento = movimiento; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public Integer getStockLoteAnterior() { return stockLoteAnterior; }
    public void setStockLoteAnterior(Integer stockLoteAnterior) { this.stockLoteAnterior = stockLoteAnterior; }
    public Integer getStockLotePosterior() { return stockLotePosterior; }
    public void setStockLotePosterior(Integer stockLotePosterior) { this.stockLotePosterior = stockLotePosterior; }
    public Integer getStockProductoAnterior() { return stockProductoAnterior; }
    public void setStockProductoAnterior(Integer stockProductoAnterior) { this.stockProductoAnterior = stockProductoAnterior; }
    public Integer getStockProductoPosterior() { return stockProductoPosterior; }
    public void setStockProductoPosterior(Integer stockProductoPosterior) { this.stockProductoPosterior = stockProductoPosterior; }
}
