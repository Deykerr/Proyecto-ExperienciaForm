package com.vircarmen.botica.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "devoluciones_venta")
public class DevolucionVenta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDevolucionVenta;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "caja_sesion_id", nullable = false)
    private CajaSesion cajaSesion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(nullable = false) private LocalDateTime fecha = LocalDateTime.now();
    @Column(nullable = false, length = 300) private String motivo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TipoDevolucionVenta tipo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EstadoDevolucion estado = EstadoDevolucion.CONFIRMADA;
    @Enumerated(EnumType.STRING) @Column(name = "metodo_reembolso", length = 20)
    private MetodoPago metodoReembolso;
    @Column(name = "referencia_reembolso", length = 100) private String referenciaReembolso;
    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal subtotal;
    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal igv;
    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal total;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;
    @OneToMany(mappedBy = "devolucionVenta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference private List<DevolucionVentaDetalle> detalles = new ArrayList<>();
    @OneToMany(mappedBy = "devolucionVenta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReembolsoVenta> reembolsos = new ArrayList<>();

    public Integer getIdDevolucionVenta() { return idDevolucionVenta; }
    public void setIdDevolucionVenta(Integer id) { this.idDevolucionVenta = id; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public CajaSesion getCajaSesion() { return cajaSesion; }
    public void setCajaSesion(CajaSesion cajaSesion) { this.cajaSesion = cajaSesion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public TipoDevolucionVenta getTipo() { return tipo; }
    public void setTipo(TipoDevolucionVenta tipo) { this.tipo = tipo; }
    public EstadoDevolucion getEstado() { return estado; }
    public void setEstado(EstadoDevolucion estado) { this.estado = estado; }
    public MetodoPago getMetodoReembolso() { return metodoReembolso; }
    public void setMetodoReembolso(MetodoPago metodo) { this.metodoReembolso = metodo; }
    public String getReferenciaReembolso() { return referenciaReembolso; }
    public void setReferenciaReembolso(String referencia) { this.referenciaReembolso = referencia; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String key) { this.idempotencyKey = key; }
    public List<DevolucionVentaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<DevolucionVentaDetalle> detalles) { this.detalles = detalles; }
    public List<ReembolsoVenta> getReembolsos() { return reembolsos; }
    public void setReembolsos(List<ReembolsoVenta> reembolsos) { this.reembolsos = reembolsos; }
}
