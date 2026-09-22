package com.vircarmen.botica.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
@Table(name = "recepciones_compra")
public class RecepcionCompra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRecepcion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;
    @Column(name = "documento_proveedor", length = 60) private String documentoProveedor;
    @Column(nullable = false) private LocalDateTime fecha = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EstadoRecepcionCompra estado = EstadoRecepcionCompra.CONFIRMADA;
    @Column(length = 500) private String observaciones;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;
    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference private List<RecepcionCompraDetalle> detalles = new ArrayList<>();

    public Integer getIdRecepcion() { return idRecepcion; }
    public void setIdRecepcion(Integer id) { this.idRecepcion = id; }
    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }
    public String getDocumentoProveedor() { return documentoProveedor; }
    public void setDocumentoProveedor(String documento) { this.documentoProveedor = documento; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public EstadoRecepcionCompra getEstado() { return estado; }
    public void setEstado(EstadoRecepcionCompra estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String key) { this.idempotencyKey = key; }
    public List<RecepcionCompraDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<RecepcionCompraDetalle> detalles) { this.detalles = detalles; }
}
