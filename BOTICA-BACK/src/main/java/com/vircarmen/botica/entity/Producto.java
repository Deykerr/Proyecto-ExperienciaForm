package com.vircarmen.botica.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(length = 200, nullable = false, unique = true)
    private String nombre;

    @Column(unique = true, length = 50)
    private String codigoBarras;

    @Column(length = 20)
    private String codigoSunat;

    @Column(length = 2)
    private String tipoAfectacionIgv = "10";

    @Column(name = "principio_activo", length = 200)
    private String principioActivo;

    @Column(length = 100)
    private String presentacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_farmaceutica", length = 30)
    private FormaFarmaceutica formaFarmaceutica;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", length = 20)
    private UnidadMedida unidadMedida;

    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "stock_actual")
    private Integer stockActual = 0;

    @Column(name = "stock_minimo")
    private Integer stockMinimo = 5;

    @Column(name = "unidades_por_presentacion")
    private Integer unidadesPorPresentacion = 1;

    @Column(name = "precio_presentacion", precision = 10, scale = 2)
    private BigDecimal precioPresentacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorio_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Laboratorio laboratorio;

    @Column(name = "requiere_receta")
    private Boolean requiereReceta = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_venta", nullable = false, length = 30)
    private CondicionVenta condicionVenta = CondicionVenta.SIN_RECETA_MEDICA;

    @Column(name = "registro_sanitario", length = 50)
    private String registroSanitario;

    @Enumerated(EnumType.STRING)
    @Column(length = 1, columnDefinition = "varchar(1)")
    private EstadoGeneral estado = EstadoGeneral.A;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lote> lotes = new ArrayList<>();

    // Getters and Setters
    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public String getCodigoSunat() { return codigoSunat; }
    public void setCodigoSunat(String codigoSunat) { this.codigoSunat = codigoSunat; }
    public String getTipoAfectacionIgv() { return tipoAfectacionIgv; }
    public void setTipoAfectacionIgv(String tipoAfectacionIgv) { this.tipoAfectacionIgv = tipoAfectacionIgv; }
    public String getPrincipioActivo() { return principioActivo; }
    public void setPrincipioActivo(String principioActivo) { this.principioActivo = principioActivo; }
    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public FormaFarmaceutica getFormaFarmaceutica() { return formaFarmaceutica; }
    public void setFormaFarmaceutica(FormaFarmaceutica formaFarmaceutica) { this.formaFarmaceutica = formaFarmaceutica; }
    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(UnidadMedida unidadMedida) { this.unidadMedida = unidadMedida; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public Integer getUnidadesPorPresentacion() { return unidadesPorPresentacion; }
    public void setUnidadesPorPresentacion(Integer unidadesPorPresentacion) { this.unidadesPorPresentacion = unidadesPorPresentacion; }
    public BigDecimal getPrecioPresentacion() { return precioPresentacion; }
    public void setPrecioPresentacion(BigDecimal precioPresentacion) { this.precioPresentacion = precioPresentacion; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Laboratorio getLaboratorio() { return laboratorio; }
    public void setLaboratorio(Laboratorio laboratorio) { this.laboratorio = laboratorio; }
    public Boolean getRequiereReceta() { return requiereReceta; }
    public void setRequiereReceta(Boolean requiereReceta) {
        this.requiereReceta = requiereReceta;
        if (Boolean.FALSE.equals(requiereReceta)) {
            this.condicionVenta = CondicionVenta.SIN_RECETA_MEDICA;
        } else if (this.condicionVenta == CondicionVenta.SIN_RECETA_MEDICA) {
            this.condicionVenta = CondicionVenta.CON_RECETA_MEDICA;
        }
    }
    public CondicionVenta getCondicionVenta() { return condicionVenta; }
    public void setCondicionVenta(CondicionVenta condicionVenta) {
        this.condicionVenta = condicionVenta == null ? CondicionVenta.SIN_RECETA_MEDICA : condicionVenta;
        this.requiereReceta = this.condicionVenta.requiereReceta();
    }
    public String getRegistroSanitario() { return registroSanitario; }
    public void setRegistroSanitario(String registroSanitario) { this.registroSanitario = registroSanitario; }
    public EstadoGeneral getEstado() { return estado; }
    public void setEstado(EstadoGeneral estado) { this.estado = estado; }
    public List<Lote> getLotes() { return lotes; }
    public void setLotes(List<Lote> lotes) { this.lotes = lotes; }
}
