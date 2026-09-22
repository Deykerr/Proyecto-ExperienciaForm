package com.vircarmen.botica.entity;

import java.time.LocalDate;
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
@Table(name = "recetas")
public class Receta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReceta;

    @Column(nullable = false, unique = true, length = 60)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "paciente_nombre", nullable = false, length = 200)
    private String pacienteNombre;

    @Column(name = "paciente_documento", length = 20)
    private String pacienteDocumento;

    @Column(name = "medico_nombre", nullable = false, length = 200)
    private String medicoNombre;

    @Column(name = "medico_colegiatura", nullable = false, length = 30)
    private String medicoColegiatura;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_venta", nullable = false, length = 30)
    private CondicionVenta condicionVenta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReceta estado = EstadoReceta.DISPONIBLE;

    @Column(name = "retenida_en")
    private LocalDateTime retenidaEn;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RecetaDetalle> detalles = new ArrayList<>();

    public Integer getIdReceta() { return idReceta; }
    public void setIdReceta(Integer idReceta) { this.idReceta = idReceta; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }
    public String getPacienteDocumento() { return pacienteDocumento; }
    public void setPacienteDocumento(String pacienteDocumento) { this.pacienteDocumento = pacienteDocumento; }
    public String getMedicoNombre() { return medicoNombre; }
    public void setMedicoNombre(String medicoNombre) { this.medicoNombre = medicoNombre; }
    public String getMedicoColegiatura() { return medicoColegiatura; }
    public void setMedicoColegiatura(String medicoColegiatura) { this.medicoColegiatura = medicoColegiatura; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public CondicionVenta getCondicionVenta() { return condicionVenta; }
    public void setCondicionVenta(CondicionVenta condicionVenta) { this.condicionVenta = condicionVenta; }
    public EstadoReceta getEstado() { return estado; }
    public void setEstado(EstadoReceta estado) { this.estado = estado; }
    public LocalDateTime getRetenidaEn() { return retenidaEn; }
    public void setRetenidaEn(LocalDateTime retenidaEn) { this.retenidaEn = retenidaEn; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public List<RecetaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<RecetaDetalle> detalles) { this.detalles = detalles; }
}
