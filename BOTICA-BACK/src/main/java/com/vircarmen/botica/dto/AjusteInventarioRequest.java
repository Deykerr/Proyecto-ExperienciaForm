    package com.vircarmen.botica.dto;

    public class AjusteInventarioRequest {
        private Integer idLote;
        private Integer cantidad;
        private String motivo;
        private Integer idUsuario;

        public Integer getIdLote() { return idLote; }
        public void setIdLote(Integer idLote) { this.idLote = idLote; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
        public Integer getIdUsuario() { return idUsuario; }
        public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    }
