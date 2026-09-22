package com.vircarmen.botica.entity;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "reembolsos_venta")
public class ReembolsoVenta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReembolso;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "devolucion_venta_id", nullable = false)
    @JsonIgnore private DevolucionVenta devolucionVenta;
    @Enumerated(EnumType.STRING) @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;
    @Column(precision = 10, scale = 2, nullable = false) private BigDecimal monto;
    @Column(length = 100) private String referencia;
    public Integer getIdReembolso() { return idReembolso; }
    public void setIdReembolso(Integer id) { this.idReembolso = id; }
    public DevolucionVenta getDevolucionVenta() { return devolucionVenta; }
    public void setDevolucionVenta(DevolucionVenta d) { this.devolucionVenta = d; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodo) { this.metodoPago = metodo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
