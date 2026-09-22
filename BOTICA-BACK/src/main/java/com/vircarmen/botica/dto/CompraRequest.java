package com.vircarmen.botica.dto;

import java.util.List;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CompraRequest {
    @NotNull(message = "El proveedor es obligatorio")
    private Integer idProveedor;

    @NotBlank(message = "El documento de compra es obligatorio")
    @Size(max = 50, message = "El documento no puede superar 50 caracteres")
    private String documento;

    @NotBlank(message = "La clave de idempotencia es obligatoria")
    @Size(max = 64, message = "La clave de idempotencia no puede superar 64 caracteres")
    private String idempotencyKey;

    @NotEmpty(message = "La compra debe contener al menos un detalle")
    @Valid
    private List<DetalleCompraRequest> detalles;

    private LocalDate fechaEsperada;

    @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
    private String observaciones;

    public Integer getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Integer idProveedor) { this.idProveedor = idProveedor; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public List<DetalleCompraRequest> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompraRequest> detalles) { this.detalles = detalles; }
    public LocalDate getFechaEsperada() { return fechaEsperada; }
    public void setFechaEsperada(LocalDate fechaEsperada) { this.fechaEsperada = fechaEsperada; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
