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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "arqueos_caja")
public class ArqueoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idArqueo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_sesion_id", nullable = false, unique = true)
    private CajaSesion cajaSesion;

    @Column(name = "total_contado", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalContado;
    @Column(name = "saldo_esperado", precision = 10, scale = 2, nullable = false)
    private BigDecimal saldoEsperado;
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal diferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoArqueo estado;

    @Column(length = 500)
    private String observaciones;
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por_id")
    private Usuario aprobadoPor;
    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;
    @Column(name = "observacion_aprobacion", length = 500)
    private String observacionAprobacion;

    @OneToMany(mappedBy = "arqueo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ArqueoDenominacion> denominaciones = new ArrayList<>();

    public Integer getIdArqueo() { return idArqueo; }
    public void setIdArqueo(Integer idArqueo) { this.idArqueo = idArqueo; }
    public CajaSesion getCajaSesion() { return cajaSesion; }
    public void setCajaSesion(CajaSesion cajaSesion) { this.cajaSesion = cajaSesion; }
    public BigDecimal getTotalContado() { return totalContado; }
    public void setTotalContado(BigDecimal totalContado) { this.totalContado = totalContado; }
    public BigDecimal getSaldoEsperado() { return saldoEsperado; }
    public void setSaldoEsperado(BigDecimal saldoEsperado) { this.saldoEsperado = saldoEsperado; }
    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
    public EstadoArqueo getEstado() { return estado; }
    public void setEstado(EstadoArqueo estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Usuario getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(Usuario aprobadoPor) { this.aprobadoPor = aprobadoPor; }
    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }
    public String getObservacionAprobacion() { return observacionAprobacion; }
    public void setObservacionAprobacion(String observacionAprobacion) { this.observacionAprobacion = observacionAprobacion; }
    public List<ArqueoDenominacion> getDenominaciones() { return denominaciones; }
    public void setDenominaciones(List<ArqueoDenominacion> denominaciones) { this.denominaciones = denominaciones; }
}
