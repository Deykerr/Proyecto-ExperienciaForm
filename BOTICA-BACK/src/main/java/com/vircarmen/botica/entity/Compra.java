package com.vircarmen.botica.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(nullable = false, length = 50)
    private String documento;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado = EstadoCompra.REGISTRADA;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<DetalleCompra> detalles = new ArrayList<>();

    // Getters and Setters
    public Integer getIdCompra() { return idCompra; }
    public void setIdCompra(Integer idCompra) { this.idCompra = idCompra; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDateTime fechaCompra) { this.fechaCompra = fechaCompra; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public EstadoCompra getEstado() { return estado; }
    public void setEstado(EstadoCompra estado) { this.estado = estado; }
    public List<DetalleCompra> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompra> detalles) { this.detalles = detalles; }
}
