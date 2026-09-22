package com.vircarmen.botica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "series_comprobante", uniqueConstraints = {
        @UniqueConstraint(name = "uk_serie_comprobante_tipo_serie", columnNames = {"tipo_comprobante", "serie"})
})
public class SerieComprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSerie;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 20)
    private TipoComprobante tipoComprobante;

    @Column(nullable = false, length = 10)
    private String serie;

    @Column(name = "ultimo_correlativo", nullable = false)
    private Long ultimoCorrelativo = 0L;

    public Integer getIdSerie() { return idSerie; }
    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public Long getUltimoCorrelativo() { return ultimoCorrelativo; }
    public void setUltimoCorrelativo(Long ultimoCorrelativo) { this.ultimoCorrelativo = ultimoCorrelativo; }
}
