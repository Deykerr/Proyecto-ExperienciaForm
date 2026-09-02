package com.vircarmen.botica.dto;

import java.util.List;

public class VentaRequest {
    private Integer idCliente;
    private String tipoComprobante;
    private List<DetalleVentaDTO> items;
    private List<PagoRequest> pagos;

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public List<DetalleVentaDTO> getItems() { return items; }
    public void setItems(List<DetalleVentaDTO> items) { this.items = items; }
    public List<PagoRequest> getPagos() { return pagos; }
    public void setPagos(List<PagoRequest> pagos) { this.pagos = pagos; }
}
