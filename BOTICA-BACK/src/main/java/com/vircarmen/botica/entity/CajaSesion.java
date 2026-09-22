package com.vircarmen.botica.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caja_sesiones")
public class CajaSesion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCajaSesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "monto_inicial", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoInicial;

    @Column(name = "monto_final", precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @Column(name = "monto_esperado", precision = 10, scale = 2)
    private BigDecimal montoEsperado;

    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia;

    @Column(name = "observaciones_cierre", length = 500)
    private String observacionesCierre;

    @Column(name = "requiere_revision", nullable = false)
    private Boolean requiereRevision = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoCaja estado = EstadoCaja.ABIERTA;

    @OneToMany(mappedBy = "cajaSesion", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<Pago> pagos = new ArrayList<>();

    @OneToMany(mappedBy = "cajaSesion", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<MovimientoCaja> movimientos = new ArrayList<>();

    public enum EstadoCaja {
        ABIERTA, CERRADA
    }

    public Integer getIdCajaSesion() { return idCajaSesion; }
    public void setIdCajaSesion(Integer idCajaSesion) { this.idCajaSesion = idCajaSesion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public BigDecimal getMontoInicial() { return montoInicial; }
    public void setMontoInicial(BigDecimal montoInicial) { this.montoInicial = montoInicial; }
    public BigDecimal getMontoFinal() { return montoFinal; }
    public void setMontoFinal(BigDecimal montoFinal) { this.montoFinal = montoFinal; }
    public BigDecimal getMontoEsperado() { return montoEsperado; }
    public void setMontoEsperado(BigDecimal montoEsperado) { this.montoEsperado = montoEsperado; }
    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
    public String getObservacionesCierre() { return observacionesCierre; }
    public void setObservacionesCierre(String observacionesCierre) { this.observacionesCierre = observacionesCierre; }
    public Boolean getRequiereRevision() { return requiereRevision; }
    public void setRequiereRevision(Boolean requiereRevision) { this.requiereRevision = requiereRevision; }
    public EstadoCaja getEstado() { return estado; }
    public void setEstado(EstadoCaja estado) { this.estado = estado; }
    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }
    public List<MovimientoCaja> getMovimientos() { return movimientos; }
    public void setMovimientos(List<MovimientoCaja> movimientos) { this.movimientos = movimientos; }
}
