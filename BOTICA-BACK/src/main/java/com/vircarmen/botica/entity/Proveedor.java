package com.vircarmen.botica.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProveedor;

    @Column(length = 11, nullable = false, unique = true)
    private String ruc;

    @Column(name = "razon_social", length = 200, nullable = false, unique = true)
    private String razonSocial;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Column(length = 250)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private EstadoGeneral estado = EstadoGeneral.A;

    public Integer getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Integer idProveedor) { this.idProveedor = idProveedor; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public EstadoGeneral getEstado() { return estado; }
    public void setEstado(EstadoGeneral estado) { this.estado = estado; }
}
