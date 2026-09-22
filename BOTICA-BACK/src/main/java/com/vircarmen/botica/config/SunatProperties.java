package com.vircarmen.botica.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SunatProperties {
    @Value("${app.sunat.enabled:false}") private boolean enabled;
    @Value("${app.sunat.mode:BETA}") private String mode;
    @Value("${app.sunat.ruc:}") private String ruc;
    @Value("${app.sunat.sol-user:}") private String solUser;
    @Value("${app.sunat.sol-password:}") private String solPassword;
    @Value("${app.sunat.certificate-path:}") private String certificatePath;
    @Value("${app.sunat.certificate-base64:}") private String certificateBase64;
    @Value("${app.sunat.certificate-password:}") private String certificatePassword;
    @Value("${app.sunat.razon-social:}") private String razonSocial;
    @Value("${app.sunat.nombre-comercial:}") private String nombreComercial;
    @Value("${app.sunat.ubigeo:}") private String ubigeo;
    @Value("${app.sunat.direccion:}") private String direccion;
    @Value("${app.sunat.departamento:LIMA}") private String departamento;
    @Value("${app.sunat.provincia:LIMA}") private String provincia;
    @Value("${app.sunat.distrito:LIMA}") private String distrito;
    @Value("${app.sunat.endpoint-beta:https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService}")
    private String endpointBeta;
    @Value("${app.sunat.endpoint-produccion:https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService}")
    private String endpointProduccion;

    public boolean isEnabled() { return enabled; }
    public String getMode() { return mode; }
    public String getRuc() { return ruc; }
    public String getSolUser() { return solUser; }
    public String getSolPassword() { return solPassword; }
    public String getCertificatePath() { return certificatePath; }
    public String getCertificateBase64() { return certificateBase64; }
    public String getCertificatePassword() { return certificatePassword; }
    public String getRazonSocial() { return razonSocial; }
    public String getNombreComercial() { return nombreComercial; }
    public String getUbigeo() { return ubigeo; }
    public String getDireccion() { return direccion; }
    public String getDepartamento() { return departamento; }
    public String getProvincia() { return provincia; }
    public String getDistrito() { return distrito; }
    public String endpoint() { return "PRODUCCION".equalsIgnoreCase(mode) ? endpointProduccion : endpointBeta; }

    public boolean configuracionCompleta() {
        return enabled && noVacio(ruc) && ruc.matches("\\d{11}") && noVacio(solUser)
                && noVacio(solPassword) && noVacio(certificatePassword)
                && (noVacio(certificatePath) || noVacio(certificateBase64))
                && noVacio(razonSocial) && noVacio(ubigeo) && noVacio(direccion);
    }

    private boolean noVacio(String valor) { return valor != null && !valor.isBlank(); }
}
