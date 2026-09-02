package com.vircarmen.botica.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "laboratorios")
public class Laboratorio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLaboratorio;

    @Column(length = 150, nullable = false, unique = true)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private EstadoGeneral estado = EstadoGeneral.A;

    public Integer getIdLaboratorio() { return idLaboratorio; }
    public void setIdLaboratorio(Integer idLaboratorio) { this.idLaboratorio = idLaboratorio; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoGeneral getEstado() { return estado; }
    public void setEstado(EstadoGeneral estado) { this.estado = estado; }
}
