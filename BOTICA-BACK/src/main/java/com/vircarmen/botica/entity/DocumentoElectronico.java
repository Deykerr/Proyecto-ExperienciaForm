package com.vircarmen.botica.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "documentos_electronicos", uniqueConstraints =
        @UniqueConstraint(name = "uk_documento_electronico_numero", columnNames = {"tipo_documento", "serie", "correlativo"}))
public class DocumentoElectronico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDocumento;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "comprobante_id", unique = true)
    private Comprobante comprobante;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "devolucion_venta_id", unique = true)
    private DevolucionVenta devolucionVenta;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resumen_diario_id")
    private ResumenDiarioSunat resumenDiario;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumentoElectronico tipoDocumento;
    @Column(nullable = false, length = 10) private String serie;
    @Column(nullable = false, length = 20) private String correlativo;
    @Column(name = "fecha_emision", nullable = false) private LocalDateTime fechaEmision = LocalDateTime.now();
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private EstadoDocumentoElectronico estado;
    @Column(columnDefinition = "TEXT") private String xml;
    @Column(name = "hash_xml", length = 64) private String hashXml;
    @Column(columnDefinition = "bytea") private byte[] cdr;
    @Column(name = "codigo_respuesta", length = 20) private String codigoRespuesta;
    @Column(name = "descripcion_respuesta", length = 1000) private String descripcionRespuesta;
    @Column(nullable = false) private Integer intentos = 0;
    @Column(name = "ultimo_intento") private LocalDateTime ultimoIntento;
    @Column(name = "siguiente_intento") private LocalDateTime siguienteIntento;

    public Integer getIdDocumento() { return idDocumento; }
    public void setIdDocumento(Integer id) { this.idDocumento = id; }
    public Comprobante getComprobante() { return comprobante; }
    public void setComprobante(Comprobante c) { this.comprobante = c; }
    public DevolucionVenta getDevolucionVenta() { return devolucionVenta; }
    public void setDevolucionVenta(DevolucionVenta d) { this.devolucionVenta = d; }
    public ResumenDiarioSunat getResumenDiario() { return resumenDiario; }
    public void setResumenDiario(ResumenDiarioSunat resumen) { this.resumenDiario = resumen; }
    public TipoDocumentoElectronico getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumentoElectronico tipo) { this.tipoDocumento = tipo; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fecha) { this.fechaEmision = fecha; }
    public EstadoDocumentoElectronico getEstado() { return estado; }
    public void setEstado(EstadoDocumentoElectronico estado) { this.estado = estado; }
    public String getXml() { return xml; }
    public void setXml(String xml) { this.xml = xml; }
    public String getHashXml() { return hashXml; }
    public void setHashXml(String hash) { this.hashXml = hash; }
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
}
