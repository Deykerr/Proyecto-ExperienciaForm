package com.vircarmen.botica.dto;

import java.util.List;

public class CompraRequest {
    private Integer idProveedor;
    private String documento;
    private List<DetalleCompraRequest> detalles;

    public Integer getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Integer idProveedor) { this.idProveedor = idProveedor; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public List<DetalleCompraRequest> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompraRequest> detalles) { this.detalles = detalles; }
}
