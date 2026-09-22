package com.vircarmen.botica.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.vircarmen.botica.config.SunatProperties;
import com.vircarmen.botica.entity.Cliente;
import com.vircarmen.botica.entity.Comprobante;
import com.vircarmen.botica.entity.DetalleVenta;
import com.vircarmen.botica.entity.DevolucionVenta;
import com.vircarmen.botica.entity.DevolucionVentaDetalle;
import com.vircarmen.botica.entity.DocumentoElectronico;
import com.vircarmen.botica.entity.ResumenDiarioSunat;
import com.vircarmen.botica.entity.TipoDocumentoElectronico;
import lombok.RequiredArgsConstructor;

/** Genera el Resumen Diario UBL 2.0 exigido para boletas y sus notas asociadas. */
@Component
@RequiredArgsConstructor
public class SunatResumenDiarioGenerator {
    private final SunatProperties properties;

    public String generar(ResumenDiarioSunat resumen, List<DocumentoElectronico> documentos) {
        StringBuilder xml = new StringBuilder(8192);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<SummaryDocuments xmlns=\"urn:sunat:names:specification:ubl:peru:schema:xsd:SummaryDocuments-1\" ")
                .append("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" ")
                .append("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" ")
                .append("xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\" ")
                .append("xmlns:sac=\"urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1\">")
                .append("<ext:UBLExtensions><ext:UBLExtension><ext:ExtensionContent/>")
                .append("</ext:UBLExtension></ext:UBLExtensions>")
                .append(tag("cbc:UBLVersionID", "2.0"))
                .append(tag("cbc:CustomizationID", "1.1"))
                .append(tag("cbc:ID", resumen.getIdentificador()))
                .append(tag("cbc:ReferenceDate", resumen.getFechaReferencia().toString()))
                .append(tag("cbc:IssueDate", resumen.getFechaGeneracion().toLocalDate().toString()))
                .append(firma()).append(emisor());

        int linea = 1;
        for (DocumentoElectronico documento : documentos) {
            xml.append(linea(documento, linea++));
        }
        return xml.append("</SummaryDocuments>").toString();
    }

    private String linea(DocumentoElectronico documento, int numeroLinea) {
        boolean boleta = documento.getTipoDocumento() == TipoDocumentoElectronico.BOLETA;
        Comprobante comprobante = boleta ? documento.getComprobante() : null;
        DevolucionVenta devolucion = boleta ? null : documento.getDevolucionVenta();
        Cliente cliente = boleta ? comprobante.getCliente() : devolucion.getVenta().getCliente();
        BigDecimal total = boleta ? comprobante.getTotal() : devolucion.getTotal();
        Map<String, Totales> grupos = boleta
                ? gruposVenta(comprobante.getVenta().getDetalles())
                : gruposDevolucion(devolucion.getDetalles());

        StringBuilder xml = new StringBuilder("<sac:SummaryDocumentsLine>")
                .append(tag("cbc:LineID", String.valueOf(numeroLinea)))
                .append(tag("cbc:DocumentTypeCode", boleta ? "03" : "07"))
                .append(tag("cbc:ID", documento.getSerie() + "-" + documento.getCorrelativo()));
        if (!boleta) {
            Comprobante original = devolucion.getVenta().getComprobante();
            xml.append("<cac:BillingReference><cac:InvoiceDocumentReference>")
                    .append(tag("cbc:ID", original.getSerie() + "-" + original.getCorrelativo()))
                    .append(tag("cbc:DocumentTypeCode", "03"))
                    .append("</cac:InvoiceDocumentReference></cac:BillingReference>");
        }
        xml.append(cliente(cliente))
                .append("<cac:Status>").append(tag("cbc:ConditionCode", "1")).append("</cac:Status>")
                .append(money("sac:TotalAmount", total));
        grupos.forEach((instruction, importe) -> xml.append("<sac:BillingPayment>")
                .append(money("cbc:PaidAmount", importe.base()))
                .append(tag("cbc:InstructionID", instruction)).append("</sac:BillingPayment>"));
        grupos.forEach((instruction, importe) -> {
            Tributo t = tributo(instruction);
            xml.append("<cac:TaxTotal>").append(money("cbc:TaxAmount", importe.igv()))
                    .append("<cac:TaxSubtotal>").append(money("cbc:TaxAmount", importe.igv()))
                    .append("<cac:TaxCategory>").append(tag("cbc:ID", t.categoria()))
                    .append(tag("cbc:Percent", t.porcentaje())).append("<cac:TaxScheme>")
                    .append(tag("cbc:ID", t.codigo())).append(tag("cbc:Name", t.nombre()))
                    .append(tag("cbc:TaxTypeCode", t.tipo()))
                    .append("</cac:TaxScheme></cac:TaxCategory></cac:TaxSubtotal></cac:TaxTotal>");
        });
        return xml.append("</sac:SummaryDocumentsLine>").toString();
    }

    private Map<String, Totales> gruposVenta(List<DetalleVenta> detalles) {
        Map<String, Totales> grupos = new LinkedHashMap<>();
        detalles.forEach(d -> acumular(grupos, instruccion(d.getTipoAfectacionIgv()),
                d.getBaseImponible(), d.getIgv()));
        return grupos;
    }

    private Map<String, Totales> gruposDevolucion(List<DevolucionVentaDetalle> detalles) {
        Map<String, Totales> grupos = new LinkedHashMap<>();
        detalles.forEach(d -> acumular(grupos, instruccion(d.getDetalleVenta().getTipoAfectacionIgv()),
                d.getBaseImponible(), d.getIgv()));
        return grupos;
    }

    private void acumular(Map<String, Totales> grupos, String clave, BigDecimal base, BigDecimal igv) {
        grupos.compute(clave, (k, v) -> v == null ? new Totales(base, igv)
                : new Totales(v.base().add(base), v.igv().add(igv)));
    }

    private String instruccion(String afectacion) {
        if (afectacion != null && afectacion.startsWith("1")) return "01";
        if (afectacion != null && afectacion.startsWith("2")) return "02";
        if (afectacion != null && afectacion.startsWith("4")) return "05";
        return "03";
    }

    private Tributo tributo(String instruccion) {
        return switch (instruccion) {
            case "01" -> new Tributo("S", "1000", "IGV", "VAT", "18.00");
            case "02" -> new Tributo("E", "9997", "EXO", "VAT", "0.00");
            case "05" -> new Tributo("Z", "9996", "GRA", "FRE", "0.00");
            default -> new Tributo("O", "9998", "INA", "FRE", "0.00");
        };
    }

    private String emisor() {
        return "<cac:AccountingSupplierParty>" + tag("cbc:CustomerAssignedAccountID", properties.getRuc())
                + tag("cbc:AdditionalAccountID", "6") + "</cac:AccountingSupplierParty>";
    }

    private String firma() {
        String comercial = properties.getNombreComercial() == null || properties.getNombreComercial().isBlank()
                ? properties.getRazonSocial() : properties.getNombreComercial();
        return "<cac:Signature>" + tag("cbc:ID", "IDSignSP")
                + "<cac:SignatoryParty><cac:PartyIdentification>" + tag("cbc:ID", properties.getRuc())
                + "</cac:PartyIdentification><cac:PartyName>" + tag("cbc:Name", comercial)
                + "</cac:PartyName></cac:SignatoryParty><cac:DigitalSignatureAttachment><cac:ExternalReference>"
                + tag("cbc:URI", "SignatureSP")
                + "</cac:ExternalReference></cac:DigitalSignatureAttachment></cac:Signature>";
    }

    private String cliente(Cliente cliente) {
        String numero = cliente == null ? "-" : cliente.getNumeroDocumento();
        String tipo = cliente == null ? "0" : tipoDocumento(cliente.getTipoDocumento());
        return "<cac:AccountingCustomerParty>" + tag("cbc:CustomerAssignedAccountID", numero)
                + tag("cbc:AdditionalAccountID", tipo) + "</cac:AccountingCustomerParty>";
    }

    private String tipoDocumento(String tipo) {
        if (tipo == null) return "0";
        return switch (tipo.trim().toUpperCase(Locale.ROOT)) {
            case "RUC" -> "6";
            case "DNI" -> "1";
            case "CARNET_EXT", "CE" -> "4";
            case "PASAPORTE" -> "7";
            default -> "0";
        };
    }

    private String tag(String nombre, String valor) {
        return "<" + nombre + ">" + esc(valor) + "</" + nombre + ">";
    }

    private String money(String nombre, BigDecimal valor) {
        return "<" + nombre + " currencyID=\"PEN\">"
                + valor.setScale(2, RoundingMode.HALF_UP).toPlainString() + "</" + nombre + ">";
    }

    private String esc(String valor) {
        return valor == null ? "" : valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private record Totales(BigDecimal base, BigDecimal igv) {}
    private record Tributo(String categoria, String codigo, String nombre, String tipo, String porcentaje) {}
}
