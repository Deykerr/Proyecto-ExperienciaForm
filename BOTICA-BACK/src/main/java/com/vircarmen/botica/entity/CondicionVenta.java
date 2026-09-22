package com.vircarmen.botica.entity;

/** Catálogo oficial DIGEMID de condición de venta (versión marzo 2026). */
public enum CondicionVenta {
    CON_RECETA_MEDICA("CRM-1"),
    CON_RECETA_MEDICA_RETENIDA("CRMR-2"),
    SIN_RECETA_MEDICA("SRM-3");

    private final String codigoDigemid;

    CondicionVenta(String codigoDigemid) {
        this.codigoDigemid = codigoDigemid;
    }

    public String getCodigoDigemid() {
        return codigoDigemid;
    }

    public boolean requiereReceta() {
        return this != SIN_RECETA_MEDICA;
    }
}
