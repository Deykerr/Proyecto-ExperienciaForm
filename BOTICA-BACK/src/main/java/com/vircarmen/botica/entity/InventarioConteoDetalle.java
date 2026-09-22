package com.vircarmen.botica.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "inventario_conteo_detalles")
public class InventarioConteoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetalleConteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteo_id", nullable = false)
    private InventarioConteo conteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "stock_sistema", nullable = false)
    private Integer stockSistema;

    @Column(name = "stock_contado", nullable = false)
    private Integer stockContado;

    @Column(nullable = false)
    private Integer diferencia;

    public Integer getIdDetalleConteo() { return idDetalleConteo; }
    public InventarioConteo getConteo() { return conteo; }
    public void setConteo(InventarioConteo conteo) { this.conteo = conteo; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public Integer getStockSistema() { return stockSistema; }
    public void setStockSistema(Integer stockSistema) { this.stockSistema = stockSistema; }
    public Integer getStockContado() { return stockContado; }
    public void setStockContado(Integer stockContado) { this.stockContado = stockContado; }
    public Integer getDiferencia() { return diferencia; }
    public void setDiferencia(Integer diferencia) { this.diferencia = diferencia; }
}
