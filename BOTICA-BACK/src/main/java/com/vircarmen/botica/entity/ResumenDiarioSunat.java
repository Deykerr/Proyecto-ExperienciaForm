package com.vircarmen.botica.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "resumenes_diarios_sunat")
public class ResumenDiarioSunat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idResumen;
    @Column(length = 25, unique = true) private String identificador;
    @Column(name = "fecha_referencia", nullable = false) private LocalDate fechaReferencia;
    @Column(name = "fecha_generacion", nullable = false) private LocalDateTime fechaGeneracion;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private EstadoResumenDiarioSunat estado;
    @Column(columnDefinition = "TEXT") private String xml;
    @Column(name = "hash_xml", length = 64) private String hashXml;
    @Column(length = 100) private String ticket;
    @Column(columnDefinition = "bytea") private byte[] cdr;
    @Column(name = "codigo_respuesta", length = 20) private String codigoRespuesta;
    @Column(name = "descripcion_respuesta", length = 1000) private String descripcionRespuesta;
    @Column(nullable = false) private Integer intentos = 0;
    @Column(name = "ultimo_intento") private LocalDateTime ultimoIntento;
    @Column(name = "siguiente_intento") private LocalDateTime siguienteIntento;
    @OneToMany(mappedBy = "resumenDiario")
    private List<DocumentoElectronico> documentos = new ArrayList<>();

    public Integer getIdResumen() { return idResumen; }
    public void setIdResumen(Integer id) { this.idResumen = id; }
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public LocalDate getFechaReferencia() { return fechaReferencia; }
    public void setFechaReferencia(LocalDate fecha) { this.fechaReferencia = fecha; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fecha) { this.fechaGeneracion = fecha; }
    public EstadoResumenDiarioSunat getEstado() { return estado; }
    public void setEstado(EstadoResumenDiarioSunat estado) { this.estado = estado; }
    public String getXml() { return xml; }
    public void setXml(String xml) { this.xml = xml; }
    public String getHashXml() { return hashXml; }
    public void setHashXml(String hash) { this.hashXml = hash; }
    public String getTicket() { return ticket; }
    public void setTicket(String ticket) { this.ticket = ticket; }
    public byte[] getCdr() { return cdr; }
    public void setCdr(byte[] cdr) { this.cdr = cdr; }
    public String getCodigoRespuesta() { return codigoRespuesta; }
    public void setCodigoRespuesta(String codigo) { this.codigoRespuesta = codigo; }
    public String getDescripcionRespuesta() { return descripcionRespuesta; }
    public void setDescripcionRespuesta(String descripcion) { this.descripcionRespuesta = descripcion; }
    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer intentos) { this.intentos = intentos; }
    public LocalDateTime getUltimoIntento() { return ultimoIntento; }
    public void setUltimoIntento(LocalDateTime fecha) { this.ultimoIntento = fecha; }
    public LocalDateTime getSiguienteIntento() { return siguienteIntento; }
    public void setSiguienteIntento(LocalDateTime fecha) { this.siguienteIntento = fecha; }
    public List<DocumentoElectronico> getDocumentos() { return documentos; }
    public void setDocumentos(List<DocumentoElectronico> documentos) { this.documentos = documentos; }
}
