package com.vircarmen.botica.entity;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "recepcion_compra_detalles")
public class RecepcionCompraDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRecepcionDetalle;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recepcion_id", nullable = false)
    @JsonBackReference private RecepcionCompra recepcion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "detalle_compra_id", nullable = false)
    private DetalleCompra detalleCompra;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "lote_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) private Lote lote;
    @Column(nullable = false) private Integer cantidad;
    @Column(name = "costo_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal costoUnitario;

    public Integer getIdRecepcionDetalle() { return idRecepcionDetalle; }
    public void setIdRecepcionDetalle(Integer id) { this.idRecepcionDetalle = id; }
    public RecepcionCompra getRecepcion() { return recepcion; }
    public void setRecepcion(RecepcionCompra r) { this.recepcion = r; }
    public DetalleCompra getDetalleCompra() { return detalleCompra; }
    public void setDetalleCompra(DetalleCompra d) { this.detalleCompra = d; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costo) { this.costoUnitario = costo; }
}
