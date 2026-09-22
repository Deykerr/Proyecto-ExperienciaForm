package com.vircarmen.botica.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimientoCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_sesion_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private CajaSesion cajaSesion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimientoCaja tipoMovimiento;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "referencia_tipo", length = 30)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Integer referenciaId;

    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    // Getters and Setters
    public Integer getIdMovimientoCaja() { return idMovimientoCaja; }
    public void setIdMovimientoCaja(Integer idMovimientoCaja) { this.idMovimientoCaja = idMovimientoCaja; }
    public CajaSesion getCajaSesion() { return cajaSesion; }
    public void setCajaSesion(CajaSesion cajaSesion) { this.cajaSesion = cajaSesion; }
    public TipoMovimientoCaja getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimientoCaja tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getReferenciaTipo() { return referenciaTipo; }
    public void setReferenciaTipo(String referenciaTipo) { this.referenciaTipo = referenciaTipo; }
    public Integer getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Integer referenciaId) { this.referenciaId = referenciaId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
