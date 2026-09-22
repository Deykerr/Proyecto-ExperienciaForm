package com.vircarmen.botica.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "series_documento_electronico", uniqueConstraints =
        @UniqueConstraint(name = "uk_serie_documento_electronico", columnNames = {"tipo_documento", "serie"}))
public class SerieDocumentoElectronico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSerie;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumentoElectronico tipoDocumento;
    @Column(nullable = false, length = 10) private String serie;
    @Column(name = "ultimo_correlativo", nullable = false) private Long ultimoCorrelativo = 0L;
    public Integer getIdSerie() { return idSerie; }
    public void setIdSerie(Integer id) { this.idSerie = id; }
    public TipoDocumentoElectronico getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumentoElectronico tipo) { this.tipoDocumento = tipo; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public Long getUltimoCorrelativo() { return ultimoCorrelativo; }
    public void setUltimoCorrelativo(Long valor) { this.ultimoCorrelativo = valor; }
}
