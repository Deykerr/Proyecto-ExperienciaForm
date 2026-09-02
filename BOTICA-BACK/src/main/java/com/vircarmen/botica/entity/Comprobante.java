package com.vircarmen.botica.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tipo_comprobante", "serie", "correlativo"})
})
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idComprobante;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 20)
    private TipoComprobante tipoComprobante;

    @Column(nullable = false, length = 10)
    private String serie;

    @Column(nullable = false, length = 20)
    private String correlativo;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal igv;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    // Getters and Setters
    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
