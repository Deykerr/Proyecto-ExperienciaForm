package com.vircarmen.botica.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimiento;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimiento tipoMovimiento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Proveedor proveedor;

    @Column(length = 300)
    private String motivo;

    @Column(name = "referencia_anulacion")
    private Integer referenciaAnulacion;

    public Integer getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(Integer idMovimiento) { this.idMovimiento = idMovimiento; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Integer getReferenciaAnulacion() { return referenciaAnulacion; }
    public void setReferenciaAnulacion(Integer referenciaAnulacion) { this.referenciaAnulacion = referenciaAnulacion; }
}
