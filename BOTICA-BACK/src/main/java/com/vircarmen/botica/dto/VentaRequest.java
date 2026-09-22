package com.vircarmen.botica.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class VentaRequest {
    private Integer idCliente;

    @NotBlank(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante;

    @NotBlank(message = "La clave de idempotencia es obligatoria")
    @Size(max = 64, message = "La clave de idempotencia no puede superar 64 caracteres")
    private String idempotencyKey;

    @Size(max = 100, message = "La referencia de receta no puede superar 100 caracteres")
    private String referenciaReceta;

    private Integer idReceta;

    @NotEmpty(message = "La venta debe contener al menos un producto")
    @Valid
    private List<DetalleVentaDTO> items;

    @NotEmpty(message = "Debe ingresar al menos un método de pago")
    @Valid
    private List<PagoRequest> pagos;

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getReferenciaReceta() { return referenciaReceta; }
    public void setReferenciaReceta(String referenciaReceta) { this.referenciaReceta = referenciaReceta; }
    public Integer getIdReceta() { return idReceta; }
    public void setIdReceta(Integer idReceta) { this.idReceta = idReceta; }
    public List<DetalleVentaDTO> getItems() { return items; }
    public void setItems(List<DetalleVentaDTO> items) { this.items = items; }
    public List<PagoRequest> getPagos() { return pagos; }
    public void setPagos(List<PagoRequest> pagos) { this.pagos = pagos; }
}
