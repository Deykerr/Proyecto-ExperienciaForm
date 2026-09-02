package com.vircarmen.botica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    @Column(length = 20, nullable = false)
    private String tipoDocumento; // DNI, RUC, CARNET_EXT

    @Column(length = 15, nullable = false, unique = true)
    private String numeroDocumento;

    @Column(length = 200, nullable = false)
    private String nombreRazonSocial;

    private String direccion;

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNombreRazonSocial() { return nombreRazonSocial; }
    public void setNombreRazonSocial(String nombreRazonSocial) { this.nombreRazonSocial = nombreRazonSocial; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}