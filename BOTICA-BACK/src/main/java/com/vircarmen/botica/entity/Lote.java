package com.vircarmen.botica.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lotes")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLote;

    @Column(name = "codigo_lote", length = 50, nullable = false)
    private String codigoLote;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "stock_inicial", nullable = false)
    private Integer stockInicial;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLote estado = EstadoLote.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnoreProperties({"lotes", "hibernateLazyInitializer", "handler"})
    private Producto producto;

    public Integer getIdLote() { return idLote; }
    public void setIdLote(Integer idLote) { this.idLote = idLote; }
    public String getCodigoLote() { return codigoLote; }
    public void setCodigoLote(String codigoLote) { this.codigoLote = codigoLote; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public Integer getStockInicial() { return stockInicial; }
    public void setStockInicial(Integer stockInicial) { this.stockInicial = stockInicial; }
    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }
    public EstadoLote getEstado() { return estado; }
    public void setEstado(EstadoLote estado) { this.estado = estado; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
}
