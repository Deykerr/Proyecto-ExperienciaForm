package com.vircarmen.botica.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "receta_detalles", uniqueConstraints =
        @UniqueConstraint(name = "uk_receta_producto", columnNames = {"receta_id", "producto_id"}))
public class RecetaDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRecetaDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    @JsonBackReference
    private Receta receta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnoreProperties({"lotes", "hibernateLazyInitializer", "handler"})
    private Producto producto;

    @Column(name = "cantidad_autorizada", nullable = false)
    private Integer cantidadAutorizada;

    @Column(name = "cantidad_dispensada", nullable = false)
    private Integer cantidadDispensada = 0;

    @Column(length = 300)
    private String indicaciones;

    public Integer getIdRecetaDetalle() { return idRecetaDetalle; }
    public void setIdRecetaDetalle(Integer idRecetaDetalle) { this.idRecetaDetalle = idRecetaDetalle; }
    public Receta getReceta() { return receta; }
    public void setReceta(Receta receta) { this.receta = receta; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Integer getCantidadAutorizada() { return cantidadAutorizada; }
    public void setCantidadAutorizada(Integer cantidadAutorizada) { this.cantidadAutorizada = cantidadAutorizada; }
    public Integer getCantidadDispensada() { return cantidadDispensada; }
    public void setCantidadDispensada(Integer cantidadDispensada) { this.cantidadDispensada = cantidadDispensada; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
}
