package com.vircarmen.botica.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
@Table(name = "devoluciones_proveedor")
public class DevolucionProveedor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDevolucionProveedor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;
    @Column(nullable = false) private LocalDateTime fecha = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(nullable = false, length = 300) private String motivo;
    @Column(name = "documento_referencia", length = 60) private String documentoReferencia;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EstadoDevolucionProveedor estado = EstadoDevolucionProveedor.CONFIRMADA;
    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal total;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;
    @OneToMany(mappedBy = "devolucionProveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference private List<DevolucionProveedorDetalle> detalles = new ArrayList<>();

    public Integer getIdDevolucionProveedor() { return idDevolucionProveedor; }
    public void setIdDevolucionProveedor(Integer id) { this.idDevolucionProveedor = id; }
    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String doc) { this.documentoReferencia = doc; }
    public EstadoDevolucionProveedor getEstado() { return estado; }
    public void setEstado(EstadoDevolucionProveedor estado) { this.estado = estado; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String key) { this.idempotencyKey = key; }
    public List<DevolucionProveedorDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<DevolucionProveedorDetalle> detalles) { this.detalles = detalles; }
}
