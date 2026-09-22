package com.vircarmen.botica.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
@Table(name = "reportes_precios_digemid")
public class ReportePrecioDigemid {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReporte;
    @Column(nullable = false, unique = true, length = 7) private String periodo;
    @Column(name = "fecha_generacion", nullable = false) private LocalDateTime fechaGeneracion = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id", nullable = false) private Usuario usuario;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EstadoReporteDigemid estado;
    @Column(name = "cantidad_productos", nullable = false) private Integer cantidadProductos;
    @Column(name = "hash_archivo", nullable = false, length = 64) private String hashArchivo;
    @Column(name = "archivo_csv", nullable = false, columnDefinition = "TEXT") private String archivoCsv;
    @Column(name = "fecha_envio") private LocalDateTime fechaEnvio;
    @Column(length = 100) private String constancia;
    @Column(length = 1000) private String observaciones;
    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference private List<ReportePrecioDigemidDetalle> detalles = new ArrayList<>();

    public Integer getIdReporte() { return idReporte; }
    public void setIdReporte(Integer id) { this.idReporte = id; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fecha) { this.fechaGeneracion = fecha; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public EstadoReporteDigemid getEstado() { return estado; }
    public void setEstado(EstadoReporteDigemid estado) { this.estado = estado; }
    public Integer getCantidadProductos() { return cantidadProductos; }
    public void setCantidadProductos(Integer cantidad) { this.cantidadProductos = cantidad; }
    public String getHashArchivo() { return hashArchivo; }
    public void setHashArchivo(String hash) { this.hashArchivo = hash; }
    public String getArchivoCsv() { return archivoCsv; }
    public void setArchivoCsv(String csv) { this.archivoCsv = csv; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fecha) { this.fechaEnvio = fecha; }
    public String getConstancia() { return constancia; }
    public void setConstancia(String constancia) { this.constancia = constancia; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public List<ReportePrecioDigemidDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<ReportePrecioDigemidDetalle> detalles) { this.detalles = detalles; }
}
