package com.vircarmen.botica.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "arqueo_denominaciones")
public class ArqueoDenominacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idArqueoDenominacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arqueo_id", nullable = false)
    @JsonBackReference
    private ArqueoCaja arqueo;
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal denominacion;
    @Column(nullable = false)
    private Integer cantidad;
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    public Integer getIdArqueoDenominacion() { return idArqueoDenominacion; }
    public void setIdArqueoDenominacion(Integer id) { this.idArqueoDenominacion = id; }
    public ArqueoCaja getArqueo() { return arqueo; }
    public void setArqueo(ArqueoCaja arqueo) { this.arqueo = arqueo; }
    public BigDecimal getDenominacion() { return denominacion; }
    public void setDenominacion(BigDecimal denominacion) { this.denominacion = denominacion; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
