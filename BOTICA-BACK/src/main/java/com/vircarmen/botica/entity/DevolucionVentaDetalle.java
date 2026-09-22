package com.vircarmen.botica.entity;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "devolucion_venta_detalles")
public class DevolucionVentaDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDevolucionDetalle;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "devolucion_venta_id", nullable = false)
    @JsonBackReference private DevolucionVenta devolucionVenta;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "detalle_venta_id", nullable = false)
    private DetalleVenta detalleVenta;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "lote_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) private Lote lote;
    @Column(nullable = false) private Integer cantidad;
    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal subtotal;
    @Column(name = "base_imponible", precision = 10, scale = 2, nullable = false) private BigDecimal baseImponible;
    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal igv;

    public Integer getIdDevolucionDetalle() { return idDevolucionDetalle; }
    public void setIdDevolucionDetalle(Integer id) { this.idDevolucionDetalle = id; }
    public DevolucionVenta getDevolucionVenta() { return devolucionVenta; }
    public void setDevolucionVenta(DevolucionVenta d) { this.devolucionVenta = d; }
    public DetalleVenta getDetalleVenta() { return detalleVenta; }
    public void setDetalleVenta(DetalleVenta d) { this.detalleVenta = d; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getBaseImponible() { return baseImponible; }
    public void setBaseImponible(BigDecimal base) { this.baseImponible = base; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
}
