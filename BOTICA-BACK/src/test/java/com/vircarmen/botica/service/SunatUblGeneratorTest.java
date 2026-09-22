package com.vircarmen.botica.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import com.vircarmen.botica.config.SunatProperties;
import com.vircarmen.botica.entity.Cliente;
import com.vircarmen.botica.entity.Comprobante;
import com.vircarmen.botica.entity.DetalleVenta;
import com.vircarmen.botica.entity.DocumentoElectronico;
import com.vircarmen.botica.entity.Lote;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.ResumenDiarioSunat;
import com.vircarmen.botica.entity.TipoComprobante;
import com.vircarmen.botica.entity.TipoDocumentoElectronico;
import com.vircarmen.botica.entity.Venta;

class SunatUblGeneratorTest {
    private SunatUblGenerator comprobanteGenerator;
    private SunatResumenDiarioGenerator resumenGenerator;

    @BeforeEach
    void configurarEmisor() {
        SunatProperties properties = new SunatProperties();
        ReflectionTestUtils.setField(properties, "ruc", "20123456789");
        ReflectionTestUtils.setField(properties, "razonSocial", "BOTICA PRUEBA SAC");
        ReflectionTestUtils.setField(properties, "nombreComercial", "BOTICA PRUEBA");
        ReflectionTestUtils.setField(properties, "ubigeo", "150101");
        ReflectionTestUtils.setField(properties, "direccion", "AV. PRUEBA 123");
        ReflectionTestUtils.setField(properties, "departamento", "LIMA");
        ReflectionTestUtils.setField(properties, "provincia", "LIMA");
        ReflectionTestUtils.setField(properties, "distrito", "LIMA");
        comprobanteGenerator = new SunatUblGenerator(properties);
        resumenGenerator = new SunatResumenDiarioGenerator(properties);
    }

    @Test
    void generaFacturaUbl21BienFormadaConTotales() throws Exception {
        DocumentoElectronico documento = documento(TipoComprobante.FACTURA, TipoDocumentoElectronico.FACTURA,
                "F001", "00000001");

        String xml = comprobanteGenerator.generar(documento);
        Document dom = parse(xml);

        assertThat(dom.getDocumentElement().getLocalName()).isEqualTo("Invoice");
        assertThat(texto(dom, "UBLVersionID")).isEqualTo("2.1");
        assertThat(texto(dom, "ID")).isEqualTo("F001-00000001");
        assertThat(texto(dom, "InvoiceTypeCode")).isEqualTo("01");
        assertThat(xml).contains("<cbc:PayableAmount currencyID=\"PEN\">11.80</cbc:PayableAmount>");
        assertThat(xml).contains("<cbc:TaxExemptionReasonCode");
    }

    @Test
    void generaResumenDiarioUbl20ParaBoletaFirmada() throws Exception {
        DocumentoElectronico boleta = documento(TipoComprobante.BOLETA, TipoDocumentoElectronico.BOLETA,
                "B001", "00000005");
        ResumenDiarioSunat resumen = new ResumenDiarioSunat();
        resumen.setIdentificador("RC-20260918-1");
        resumen.setFechaReferencia(boleta.getFechaEmision().toLocalDate());
        resumen.setFechaGeneracion(LocalDateTime.of(2026, 9, 18, 20, 0));

        String xml = resumenGenerator.generar(resumen, List.of(boleta));
        Document dom = parse(xml);

        assertThat(dom.getDocumentElement().getLocalName()).isEqualTo("SummaryDocuments");
        assertThat(texto(dom, "UBLVersionID")).isEqualTo("2.0");
        assertThat(texto(dom, "ReferenceDate")).isEqualTo(boleta.getFechaEmision().toLocalDate().toString());
        assertThat(xml).contains("<cbc:DocumentTypeCode>03</cbc:DocumentTypeCode>");
        assertThat(xml).contains("<sac:TotalAmount currencyID=\"PEN\">11.80</sac:TotalAmount>");
        assertThat(xml).contains("<cbc:InstructionID>01</cbc:InstructionID>");
    }

    private DocumentoElectronico documento(TipoComprobante tipoComprobante,
            TipoDocumentoElectronico tipoDocumento, String serie, String correlativo) {
        Producto producto = new Producto();
        producto.setIdProducto(10);
        producto.setNombre("Paracetamol 500 mg & prueba");
        producto.setCodigoSunat("51142001");
        Lote lote = new Lote();
        lote.setIdLote(20);
        lote.setCodigoLote("L-001");
        lote.setProducto(producto);
        DetalleVenta detalle = new DetalleVenta();
        detalle.setCantidad(1);
        detalle.setLote(lote);
        detalle.setSubtotal(new BigDecimal("11.80"));
        detalle.setBaseImponible(new BigDecimal("10.00"));
        detalle.setIgv(new BigDecimal("1.80"));
        detalle.setTipoAfectacionIgv("10");
        detalle.setPrecioUnitario(new BigDecimal("11.80"));

        Cliente cliente = new Cliente();
        cliente.setTipoDocumento(tipoComprobante == TipoComprobante.FACTURA ? "RUC" : "DNI");
        cliente.setNumeroDocumento(tipoComprobante == TipoComprobante.FACTURA ? "20987654321" : "12345678");
        cliente.setNombreRazonSocial("CLIENTE DE PRUEBA");
        cliente.setDireccion("JR. CLIENTE 456");
        Venta venta = new Venta();
        venta.setIdVenta(1);
        venta.setCliente(cliente);
        venta.setSubtotal(new BigDecimal("10.00"));
        venta.setIgv(new BigDecimal("1.80"));
        venta.setTotal(new BigDecimal("11.80"));
        detalle.setVenta(venta);
        venta.getDetalles().add(detalle);

        Comprobante comprobante = new Comprobante();
        comprobante.setIdComprobante(2);
        comprobante.setVenta(venta);
        comprobante.setCliente(cliente);
        comprobante.setTipoComprobante(tipoComprobante);
        comprobante.setSerie(serie);
        comprobante.setCorrelativo(correlativo);
        comprobante.setFechaEmision(LocalDateTime.of(2026, 9, 18, 19, 30));
        comprobante.setSubtotal(new BigDecimal("10.00"));
        comprobante.setIgv(new BigDecimal("1.80"));
        comprobante.setTotal(new BigDecimal("11.80"));
        venta.setComprobante(comprobante);

        DocumentoElectronico documento = new DocumentoElectronico();
        documento.setComprobante(comprobante);
        documento.setTipoDocumento(tipoDocumento);
        documento.setSerie(serie);
        documento.setCorrelativo(correlativo);
        documento.setFechaEmision(comprobante.getFechaEmision());
        return documento;
    }

    private Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private String texto(Document document, String localName) {
        return document.getElementsByTagNameNS("*", localName).item(0).getTextContent();
    }
}
