package com.vircarmen.botica.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "reporte_precios_digemid_detalles")
public class ReportePrecioDigemidDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer idDetalle;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reporte_id", nullable = false)
    @JsonBackReference private ReportePrecioDigemid reporte;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "producto_id", nullable = false) private Producto producto;
    @Column(name = "registro_sanitario", nullable = false, length = 50) private String registroSanitario;
    @Column(name = "nombre_producto", nullable = false, length = 200) private String nombreProducto;
    @Column(length = 100) private String presentacion;
    @Column(name = "precio_venta_publico", precision = 10, scale = 2, nullable = false) private BigDecimal precioVentaPublico;
    @Column(name = "stock_disponible", nullable = false) private Integer stockDisponible;
    @Column(name = "fecha_vigencia", nullable = false) private LocalDate fechaVigencia;
    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer id) { this.idDetalle = id; }
    public ReportePrecioDigemid getReporte() { return reporte; }
    public void setReporte(ReportePrecioDigemid reporte) { this.reporte = reporte; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getRegistroSanitario() { return registroSanitario; }
    public void setRegistroSanitario(String valor) { this.registroSanitario = valor; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombre) { this.nombreProducto = nombre; }
    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public BigDecimal getPrecioVentaPublico() { return precioVentaPublico; }
    public void setPrecioVentaPublico(BigDecimal precio) { this.precioVentaPublico = precio; }
    public Integer getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(Integer stock) { this.stockDisponible = stock; }
    public LocalDate getFechaVigencia() { return fechaVigencia; }
    public void setFechaVigencia(LocalDate fecha) { this.fechaVigencia = fecha; }
}
