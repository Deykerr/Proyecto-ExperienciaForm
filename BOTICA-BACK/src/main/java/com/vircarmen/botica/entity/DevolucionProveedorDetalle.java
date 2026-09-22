package com.vircarmen.botica.entity;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "devolucion_proveedor_detalles")
public class DevolucionProveedorDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDevolucionDetalle;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "devolucion_proveedor_id", nullable = false)
    @JsonBackReference private DevolucionProveedor devolucionProveedor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "detalle_compra_id", nullable = false)
    private DetalleCompra detalleCompra;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "lote_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) private Lote lote;
    @Column(nullable = false) private Integer cantidad;
    @Column(name = "costo_unitario", precision = 10, scale = 2, nullable = false) private BigDecimal costoUnitario;
    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal subtotal;

    public Integer getIdDevolucionDetalle() { return idDevolucionDetalle; }
    public void setIdDevolucionDetalle(Integer id) { this.idDevolucionDetalle = id; }
    public DevolucionProveedor getDevolucionProveedor() { return devolucionProveedor; }
    public void setDevolucionProveedor(DevolucionProveedor d) { this.devolucionProveedor = d; }
    public DetalleCompra getDetalleCompra() { return detalleCompra; }
    public void setDetalleCompra(DetalleCompra d) { this.detalleCompra = d; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costo) { this.costoUnitario = costo; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
