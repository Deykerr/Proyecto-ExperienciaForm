package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.vircarmen.botica.config.SunatProperties;
import com.vircarmen.botica.entity.Cliente;
import com.vircarmen.botica.entity.Comprobante;
import com.vircarmen.botica.entity.DetalleVenta;
import com.vircarmen.botica.entity.DevolucionVenta;
import com.vircarmen.botica.entity.DevolucionVentaDetalle;
import com.vircarmen.botica.entity.DocumentoElectronico;
import com.vircarmen.botica.entity.TipoComprobante;
import com.vircarmen.botica.entity.TipoDevolucionVenta;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SunatUblGenerator {
    private static final String NS = "xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:%s-2\" "
            + "xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" "
            + "xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" "
            + "xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\"";

    private final SunatProperties properties;

    public String generar(DocumentoElectronico documento) {
        return documento.getComprobante() != null
                ? factura(documento, documento.getComprobante())
                : notaCredito(documento, documento.getDevolucionVenta());
    }

    private String factura(DocumentoElectronico documento, Comprobante comprobante) {
        StringBuilder xml = new StringBuilder(8192);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<Invoice ").append(NS.formatted("Invoice")).append(">")
                .append(extension()).append(tag("cbc:UBLVersionID", "2.1"))
                .append(tag("cbc:CustomizationID", "2.0"))
                .append(tag("cbc:ID", numero(documento)))
                .append(tag("cbc:IssueDate", documento.getFechaEmision().toLocalDate().toString()))
                .append(tag("cbc:IssueTime", documento.getFechaEmision().toLocalTime().withNano(0).toString()))
                .append("<cbc:InvoiceTypeCode listAgencyName=\"PE:SUNAT\" listName=\"Tipo de Documento\" "
                        + "listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01\" listID=\"0101\">")
                .append(comprobante.getTipoComprobante() == TipoComprobante.FACTURA ? "01" : "03")
                .append("</cbc:InvoiceTypeCode>")
                .append("<cbc:DocumentCurrencyCode listID=\"ISO 4217 Alpha\" listName=\"Currency\" "
                        + "listAgencyName=\"United Nations Economic Commission for Europe\">PEN</cbc:DocumentCurrencyCode>")
                .append(tag("cbc:LineCountNumeric", String.valueOf(comprobante.getVenta().getDetalles().size())))
                .append(signatureMetadata(documento)).append(proveedor()).append(cliente(comprobante.getCliente()))
                .append("<cac:PaymentTerms><cbc:ID>FormaPago</cbc:ID><cbc:PaymentMeansID>Contado</cbc:PaymentMeansID></cac:PaymentTerms>")
                .append(totalesTributos(comprobante)).append(totalesMonetarios(comprobante));

        int linea = 1;
        for (DetalleVenta detalle : comprobante.getVenta().getDetalles()) {
            BigDecimal cantidad = BigDecimal.valueOf(detalle.getCantidad());
            BigDecimal precioNeto = detalle.getBaseImponible().divide(cantidad, 6, RoundingMode.HALF_UP);
            BigDecimal precioConImpuesto = detalle.getSubtotal().divide(cantidad, 6, RoundingMode.HALF_UP);
            xml.append("<cac:InvoiceLine>").append(tag("cbc:ID", String.valueOf(linea++)))
                    .append("<cbc:InvoicedQuantity unitCode=\"").append(unidad(detalle)).append("\">")
                    .append(detalle.getCantidad()).append("</cbc:InvoicedQuantity>")
                    .append(money("cbc:LineExtensionAmount", detalle.getBaseImponible()))
                    .append("<cac:PricingReference><cac:AlternativeConditionPrice>")
                    .append(money6("cbc:PriceAmount", precioConImpuesto))
                    .append("<cbc:PriceTypeCode listName=\"Tipo de Precio\" listAgencyName=\"PE:SUNAT\" "
                            + "listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16\">01</cbc:PriceTypeCode>")
                    .append("</cac:AlternativeConditionPrice></cac:PricingReference>")
                    .append(tributoLinea(detalle.getBaseImponible(), detalle.getIgv(),
                            detalle.getTipoAfectacionIgv()))
                    .append("<cac:Item>").append(tag("cbc:Description", detalle.getLote().getProducto().getNombre()))
                    .append("<cac:SellersItemIdentification>")
                    .append(tag("cbc:ID", codigoProducto(detalle))).append("</cac:SellersItemIdentification></cac:Item>")
                    .append("<cac:Price>").append(money6("cbc:PriceAmount", precioNeto))
                    .append("</cac:Price></cac:InvoiceLine>");
        }
        return xml.append("</Invoice>").toString();
    }

    private String notaCredito(DocumentoElectronico documento, DevolucionVenta devolucion) {
        Comprobante original = devolucion.getVenta().getComprobante();
        String codigoDocumentoOriginal = original.getTipoComprobante() == TipoComprobante.FACTURA ? "01" : "03";
        String motivoCodigo = devolucion.getTipo() == TipoDevolucionVenta.ANULACION ? "01" : "07";
        StringBuilder xml = new StringBuilder(8192);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<CreditNote ").append(NS.formatted("CreditNote")).append(">")
                .append(extension()).append(tag("cbc:UBLVersionID", "2.1"))
                .append(tag("cbc:CustomizationID", "2.0"))
                .append(tag("cbc:ID", numero(documento)))
                .append(tag("cbc:IssueDate", documento.getFechaEmision().toLocalDate().toString()))
                .append("<cbc:DocumentCurrencyCode listID=\"ISO 4217 Alpha\" listName=\"Currency\" "
                        + "listAgencyName=\"United Nations Economic Commission for Europe\">PEN</cbc:DocumentCurrencyCode>")
                .append("<cac:DiscrepancyResponse>").append(tag("cbc:ReferenceID", original.getSerie() + "-" + original.getCorrelativo()))
                .append("<cbc:ResponseCode listAgencyName=\"PE:SUNAT\" listName=\"Tipo de nota de credito\" "
                        + "listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo09\">")
                .append(motivoCodigo).append("</cbc:ResponseCode>").append(tag("cbc:Description", devolucion.getMotivo()))
                .append("</cac:DiscrepancyResponse><cac:BillingReference><cac:InvoiceDocumentReference>")
                .append(tag("cbc:ID", original.getSerie() + "-" + original.getCorrelativo()))
                .append("<cbc:DocumentTypeCode listAgencyName=\"PE:SUNAT\" listName=\"Tipo de Documento\" "
                        + "listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01\">")
                .append(codigoDocumentoOriginal).append("</cbc:DocumentTypeCode>")
                .append("</cac:InvoiceDocumentReference></cac:BillingReference>")
                .append(signatureMetadata(documento)).append(proveedor()).append(cliente(devolucion.getVenta().getCliente()))
                .append(totalesTributos(devolucion)).append("<cac:LegalMonetaryTotal>")
                .append(money("cbc:LineExtensionAmount", devolucion.getSubtotal()))
                .append(money("cbc:TaxInclusiveAmount", devolucion.getTotal()))
                .append(money("cbc:PayableAmount", devolucion.getTotal())).append("</cac:LegalMonetaryTotal>");
        int linea = 1;
        for (DevolucionVentaDetalle detalle : devolucion.getDetalles()) {
            BigDecimal cantidad = BigDecimal.valueOf(detalle.getCantidad());
            BigDecimal neto = detalle.getBaseImponible().divide(cantidad, 6, RoundingMode.HALF_UP);
            BigDecimal bruto = detalle.getSubtotal().divide(cantidad, 6, RoundingMode.HALF_UP);
            String afectacion = detalle.getDetalleVenta().getTipoAfectacionIgv();
            xml.append("<cac:CreditNoteLine>").append(tag("cbc:ID", String.valueOf(linea++)))
                    .append("<cbc:CreditedQuantity unitCode=\"").append(unidad(detalle.getDetalleVenta())).append("\">")
                    .append(detalle.getCantidad()).append("</cbc:CreditedQuantity>")
                    .append(money("cbc:LineExtensionAmount", detalle.getBaseImponible()))
                    .append("<cac:PricingReference><cac:AlternativeConditionPrice>")
                    .append(money6("cbc:PriceAmount", bruto)).append(tag("cbc:PriceTypeCode", "01"))
                    .append("</cac:AlternativeConditionPrice></cac:PricingReference>")
                    .append(tributoLinea(detalle.getBaseImponible(), detalle.getIgv(), afectacion))
                    .append("<cac:Item>").append(tag("cbc:Description", detalle.getLote().getProducto().getNombre()))
                    .append("<cac:SellersItemIdentification>").append(tag("cbc:ID", codigoProducto(detalle.getDetalleVenta())))
                    .append("</cac:SellersItemIdentification></cac:Item><cac:Price>")
                    .append(money6("cbc:PriceAmount", neto)).append("</cac:Price></cac:CreditNoteLine>");
        }
        return xml.append("</CreditNote>").toString();
    }

    private String totalesTributos(Comprobante comprobante) {
        Map<String, Totales> grupos = new LinkedHashMap<>();
        for (DetalleVenta d : comprobante.getVenta().getDetalles()) {
            grupos.compute(d.getTipoAfectacionIgv(), (k, v) -> v == null
                    ? new Totales(d.getBaseImponible(), d.getIgv())
                    : new Totales(v.base().add(d.getBaseImponible()), v.impuesto().add(d.getIgv())));
        }
        return totalesTributos(grupos, comprobante.getIgv());
    }

    private String totalesTributos(DevolucionVenta devolucion) {
        Map<String, Totales> grupos = new LinkedHashMap<>();
        devolucion.getDetalles().forEach(d -> {
            String afectacion = d.getDetalleVenta().getTipoAfectacionIgv();
            grupos.compute(afectacion, (k, v) -> v == null ? new Totales(d.getBaseImponible(), d.getIgv())
                    : new Totales(v.base().add(d.getBaseImponible()), v.impuesto().add(d.getIgv())));
        });
        return totalesTributos(grupos, devolucion.getIgv());
    }

    private String totalesTributos(Map<String, Totales> grupos, BigDecimal totalIgv) {
        StringBuilder xml = new StringBuilder("<cac:TaxTotal>").append(money("cbc:TaxAmount", totalIgv));
        grupos.forEach((afectacion, total) -> {
            Tributo tributo = tributo(afectacion);
            xml.append("<cac:TaxSubtotal>").append(money("cbc:TaxableAmount", total.base()))
                    .append(money("cbc:TaxAmount", total.impuesto())).append("<cac:TaxCategory>")
                    .append(tag("cbc:ID", tributo.categoria())).append("<cac:TaxScheme>")
                    .append(tag("cbc:ID", tributo.codigo())).append(tag("cbc:Name", tributo.nombre()))
                    .append(tag("cbc:TaxTypeCode", tributo.tipo())).append("</cac:TaxScheme></cac:TaxCategory></cac:TaxSubtotal>");
        });
        return xml.append("</cac:TaxTotal>").toString();
    }

    private String tributoLinea(BigDecimal base, BigDecimal impuesto, String afectacion) {
        Tributo tributo = tributo(afectacion);
        return "<cac:TaxTotal>" + money("cbc:TaxAmount", impuesto) + "<cac:TaxSubtotal>"
                + money("cbc:TaxableAmount", base) + money("cbc:TaxAmount", impuesto)
                + "<cac:TaxCategory>" + tag("cbc:ID", tributo.categoria())
                + tag("cbc:Percent", tributo.porcentaje())
                + "<cbc:TaxExemptionReasonCode listAgencyName=\"PE:SUNAT\" listName=\"Afectacion del IGV\" "
                + "listURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07\">" + esc(afectacion)
                + "</cbc:TaxExemptionReasonCode><cac:TaxScheme>" + tag("cbc:ID", tributo.codigo())
                + tag("cbc:Name", tributo.nombre()) + tag("cbc:TaxTypeCode", tributo.tipo())
                + "</cac:TaxScheme></cac:TaxCategory></cac:TaxSubtotal></cac:TaxTotal>";
    }

    private String totalesMonetarios(Comprobante c) {
        return "<cac:LegalMonetaryTotal>" + money("cbc:LineExtensionAmount", c.getSubtotal())
                + money("cbc:TaxInclusiveAmount", c.getTotal()) + money("cbc:PayableAmount", c.getTotal())
                + "</cac:LegalMonetaryTotal>";
    }

    private String proveedor() {
        return "<cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification>"
                + "<cbc:ID schemeID=\"6\" schemeName=\"Documento de Identidad\" schemeAgencyName=\"PE:SUNAT\" "
                + "schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">" + esc(properties.getRuc()) + "</cbc:ID>"
                + "</cac:PartyIdentification><cac:PartyName>" + tag("cbc:Name", nombreComercial()) + "</cac:PartyName>"
                + "<cac:PartyLegalEntity>" + tag("cbc:RegistrationName", properties.getRazonSocial())
                + "<cac:RegistrationAddress>" + tag("cbc:ID", properties.getUbigeo())
                + tag("cbc:AddressTypeCode", "0000") + tag("cbc:CityName", properties.getProvincia())
                + tag("cbc:CountrySubentity", properties.getDepartamento())
                + tag("cbc:District", properties.getDistrito())
                + "<cac:AddressLine>" + tag("cbc:Line", properties.getDireccion()) + "</cac:AddressLine>"
                + "<cac:Country><cbc:IdentificationCode>PE</cbc:IdentificationCode></cac:Country>"
                + "</cac:RegistrationAddress></cac:PartyLegalEntity></cac:Party></cac:AccountingSupplierParty>";
    }

    private String cliente(Cliente cliente) {
        String numero = cliente == null ? "-" : cliente.getNumeroDocumento();
        String tipo = cliente == null ? "0" : tipoDocumento(cliente.getTipoDocumento());
        String nombre = cliente == null ? "CLIENTES VARIOS" : cliente.getNombreRazonSocial();
        String direccion = cliente == null ? null : cliente.getDireccion();
        return "<cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification>"
                + "<cbc:ID schemeID=\"" + tipo + "\" schemeName=\"Documento de Identidad\" "
                + "schemeAgencyName=\"PE:SUNAT\" schemeURI=\"urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06\">"
                + esc(numero) + "</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity>"
                + tag("cbc:RegistrationName", nombre)
                + (direccion == null || direccion.isBlank() ? "" : "<cac:RegistrationAddress><cac:AddressLine>"
                    + tag("cbc:Line", direccion) + "</cac:AddressLine><cac:Country><cbc:IdentificationCode>PE</cbc:IdentificationCode>"
                    + "</cac:Country></cac:RegistrationAddress>")
                + "</cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty>";
    }

    private String signatureMetadata(DocumentoElectronico d) {
        return "<cac:Signature>" + tag("cbc:ID", "IDSignSP")
                + "<cac:SignatoryParty><cac:PartyIdentification>" + tag("cbc:ID", properties.getRuc())
                + "</cac:PartyIdentification><cac:PartyName>" + tag("cbc:Name", properties.getRazonSocial())
                + "</cac:PartyName></cac:SignatoryParty><cac:DigitalSignatureAttachment>"
                + "<cac:ExternalReference>" + tag("cbc:URI", "SignatureSP")
                + "</cac:ExternalReference></cac:DigitalSignatureAttachment></cac:Signature>";
    }

    private String extension() {
        return "<ext:UBLExtensions><ext:UBLExtension><ext:ExtensionContent/></ext:UBLExtension></ext:UBLExtensions>";
    }

    private Tributo tributo(String afectacion) {
        if (afectacion != null && afectacion.startsWith("1")) return new Tributo("S", "1000", "IGV", "VAT", "18.00");
        if (afectacion != null && afectacion.startsWith("2")) return new Tributo("E", "9997", "EXO", "VAT", "0.00");
        if (afectacion != null && afectacion.startsWith("4")) return new Tributo("Z", "9996", "GRA", "FRE", "0.00");
        return new Tributo("O", "9998", "INA", "FRE", "0.00");
    }

    private String numero(DocumentoElectronico d) { return d.getSerie() + "-" + d.getCorrelativo(); }
    private String nombreComercial() { return properties.getNombreComercial() == null || properties.getNombreComercial().isBlank()
            ? properties.getRazonSocial() : properties.getNombreComercial(); }
    private String codigoProducto(DetalleVenta d) { return d.getLote().getProducto().getCodigoSunat() == null
            || d.getLote().getProducto().getCodigoSunat().isBlank()
            ? String.valueOf(d.getLote().getProducto().getIdProducto()) : d.getLote().getProducto().getCodigoSunat(); }
    private String unidad(DetalleVenta d) { return "NIU"; }
    private String tipoDocumento(String tipo) {
        if (tipo == null) return "0";
        return switch (tipo.toUpperCase()) { case "RUC" -> "6"; case "DNI" -> "1";
            case "CARNET_EXT", "CE" -> "4"; case "PASAPORTE" -> "7"; default -> "0"; };
    }
    private String tag(String nombre, String valor) { return "<" + nombre + ">" + esc(valor) + "</" + nombre + ">"; }
    private String money(String nombre, BigDecimal valor) { return "<" + nombre + " currencyID=\"PEN\">"
            + valor.setScale(2, RoundingMode.HALF_UP).toPlainString() + "</" + nombre + ">"; }
    private String money6(String nombre, BigDecimal valor) { return "<" + nombre + " currencyID=\"PEN\">"
            + valor.setScale(6, RoundingMode.HALF_UP).toPlainString() + "</" + nombre + ">"; }
    private String esc(String valor) { return valor == null ? "" : valor.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;"); }
    private record Tributo(String categoria, String codigo, String nombre, String tipo, String porcentaje) {}
    private record Totales(BigDecimal base, BigDecimal impuesto) {}
}
