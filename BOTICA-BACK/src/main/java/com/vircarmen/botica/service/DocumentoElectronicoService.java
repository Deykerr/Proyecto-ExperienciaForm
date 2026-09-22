package com.vircarmen.botica.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.config.SunatProperties;
import com.vircarmen.botica.dto.DocumentoElectronicoDTO;
import com.vircarmen.botica.entity.Comprobante;
import com.vircarmen.botica.entity.DevolucionVenta;
import com.vircarmen.botica.entity.DocumentoElectronico;
import com.vircarmen.botica.entity.EstadoDocumentoElectronico;
import com.vircarmen.botica.entity.SerieDocumentoElectronico;
import com.vircarmen.botica.entity.TipoComprobante;
import com.vircarmen.botica.entity.TipoDocumentoElectronico;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.DocumentoElectronicoRepository;
import com.vircarmen.botica.repository.SerieDocumentoElectronicoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentoElectronicoService {
    private final DocumentoElectronicoRepository documentoRepository;
    private final SerieDocumentoElectronicoRepository serieRepository;
    private final SunatProperties properties;
    private final SunatUblGenerator ublGenerator;
    private final SunatXmlSigner xmlSigner;
    private final SunatSoapClient soapClient;

    @Transactional
    public DocumentoElectronico encolarComprobante(Comprobante comprobante) {
        if (comprobante.getTipoComprobante() == TipoComprobante.TICKET) return null;
        DocumentoElectronico existente = documentoRepository
                .findByComprobanteIdComprobante(comprobante.getIdComprobante()).orElse(null);
        if (existente != null) return existente;
        DocumentoElectronico documento = new DocumentoElectronico();
        documento.setComprobante(comprobante);
        documento.setTipoDocumento(comprobante.getTipoComprobante() == TipoComprobante.FACTURA
                ? TipoDocumentoElectronico.FACTURA : TipoDocumentoElectronico.BOLETA);
        documento.setSerie(comprobante.getSerie());
        documento.setCorrelativo(comprobante.getCorrelativo());
        documento.setFechaEmision(comprobante.getFechaEmision());
        documento.setEstado(estadoInicial());
        return documentoRepository.save(documento);
    }

    @Transactional
    public DocumentoElectronico encolarNotaCredito(DevolucionVenta devolucion) {
        Comprobante original = devolucion.getVenta().getComprobante();
        if (original == null || original.getTipoComprobante() == TipoComprobante.TICKET) return null;
        DocumentoElectronico existente = documentoRepository
                .findByDevolucionVentaIdDevolucionVenta(devolucion.getIdDevolucionVenta()).orElse(null);
        if (existente != null) return existente;
        String serieCodigo = original.getTipoComprobante() == TipoComprobante.FACTURA ? "FC01" : "BC01";
        SerieDocumentoElectronico serie = serieRepository
                .findByTipoDocumentoAndSerie(TipoDocumentoElectronico.NOTA_CREDITO, serieCodigo)
                .orElseThrow(() -> new BusinessException("No existe la serie electrónica " + serieCodigo + "."));
        serie.setUltimoCorrelativo(serie.getUltimoCorrelativo() + 1);
        serieRepository.save(serie);
        DocumentoElectronico documento = new DocumentoElectronico();
        documento.setDevolucionVenta(devolucion);
        documento.setTipoDocumento(TipoDocumentoElectronico.NOTA_CREDITO);
        documento.setSerie(serieCodigo);
        documento.setCorrelativo(String.format("%08d", serie.getUltimoCorrelativo()));
        documento.setFechaEmision(LocalDateTime.now());
        documento.setEstado(estadoInicial());
        return documentoRepository.save(documento);
    }

    @Transactional
    public DocumentoElectronicoDTO procesar(Integer id) {
        DocumentoElectronico documento = documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento electrónico no encontrado."));
        if (documento.getResumenDiario() != null) {
            return map(documento);
        }
        if (!properties.configuracionCompleta()) {
            documento.setEstado(EstadoDocumentoElectronico.CONFIGURACION_PENDIENTE);
            documento.setDescripcionRespuesta("Faltan credenciales, datos del emisor o certificado digital SUNAT.");
            return map(documentoRepository.save(documento));
        }
        if (documento.getEstado() == EstadoDocumentoElectronico.ACEPTADO
                || documento.getEstado() == EstadoDocumentoElectronico.OBSERVADO) {
            return map(documento);
        }
        if (documento.getEstado() == EstadoDocumentoElectronico.FIRMADO
                && seInformaEnResumenDiario(documento)) {
            return map(documento);
        }
        if (!documentoOriginalAceptado(documento)) {
            return map(documentoRepository.save(documento));
        }
        documento.setIntentos(documento.getIntentos() + 1);
        documento.setUltimoIntento(LocalDateTime.now());
        documento.setSiguienteIntento(null);
        try {
            String xml = ublGenerator.generar(documento);
            documento.setXml(xml);
            documento.setEstado(EstadoDocumentoElectronico.GENERADO);
            documentoRepository.saveAndFlush(documento);

            String firmado = xmlSigner.firmar(xml);
            documento.setXml(firmado);
            documento.setHashXml(sha256(firmado));
            documento.setEstado(EstadoDocumentoElectronico.FIRMADO);
            documentoRepository.saveAndFlush(documento);

            if (seInformaEnResumenDiario(documento)) {
                documento.setDescripcionRespuesta("XML firmado; pendiente de envío mediante Resumen Diario SUNAT.");
                return map(documentoRepository.save(documento));
            }

            String nombre = properties.getRuc() + "-" + codigoSunat(documento) + "-"
                    + documento.getSerie() + "-" + documento.getCorrelativo();
            documento.setEstado(EstadoDocumentoElectronico.ENVIADO);
            documentoRepository.saveAndFlush(documento);
            SunatSoapClient.Respuesta respuesta = soapClient.enviar(nombre, firmado);
            documento.setCdr(respuesta.cdr());
            documento.setCodigoRespuesta(respuesta.codigo());
            documento.setDescripcionRespuesta(recortar(respuesta.descripcion(), 1000));
            documento.setEstado(respuesta.aceptada()
                    ? respuesta.observada() ? EstadoDocumentoElectronico.OBSERVADO
                    : EstadoDocumentoElectronico.ACEPTADO
                    : EstadoDocumentoElectronico.RECHAZADO);
        } catch (Exception ex) {
            documento.setEstado(EstadoDocumentoElectronico.ERROR);
            documento.setDescripcionRespuesta(recortar(ex.getMessage() == null
                    ? ex.getClass().getSimpleName() : ex.getMessage(), 1000));
            long minutos = Math.min(60, 1L << Math.min(documento.getIntentos(), 6));
            documento.setSiguienteIntento(LocalDateTime.now().plusMinutes(minutos));
        }
        return map(documentoRepository.save(documento));
    }

    private boolean documentoOriginalAceptado(DocumentoElectronico documento) {
        if (documento.getTipoDocumento() != TipoDocumentoElectronico.NOTA_CREDITO) return true;
        Comprobante original = documento.getDevolucionVenta().getVenta().getComprobante();
        DocumentoElectronico origen = documentoRepository
                .findByComprobanteIdComprobante(original.getIdComprobante()).orElse(null);
        if (origen == null) {
            documento.setEstado(EstadoDocumentoElectronico.ERROR);
            documento.setDescripcionRespuesta("No se encontró el documento electrónico original.");
            documento.setSiguienteIntento(LocalDateTime.now().plusMinutes(5));
            return false;
        }
        if (origen.getEstado() == EstadoDocumentoElectronico.ACEPTADO
                || origen.getEstado() == EstadoDocumentoElectronico.OBSERVADO) {
            return true;
        }
        if (origen.getEstado() == EstadoDocumentoElectronico.RECHAZADO) {
            documento.setEstado(EstadoDocumentoElectronico.RECHAZADO);
            documento.setDescripcionRespuesta("No se puede emitir la nota: el comprobante original fue rechazado por SUNAT.");
            documento.setSiguienteIntento(null);
            return false;
        }
        documento.setEstado(EstadoDocumentoElectronico.PENDIENTE);
        documento.setDescripcionRespuesta("La nota espera la CDR de aceptación del comprobante original.");
        documento.setSiguienteIntento(LocalDateTime.now().plusMinutes(1));
        return false;
    }

    private boolean seInformaEnResumenDiario(DocumentoElectronico documento) {
        if (documento.getTipoDocumento() == TipoDocumentoElectronico.BOLETA) return true;
        return documento.getTipoDocumento() == TipoDocumentoElectronico.NOTA_CREDITO
                && documento.getDevolucionVenta().getVenta().getComprobante().getTipoComprobante()
                    == TipoComprobante.BOLETA;
    }

    @Scheduled(fixedDelayString = "${app.sunat.procesamiento-ms:60000}")
    @Transactional
    public void procesarPendientes() {
        if (!properties.configuracionCompleta()) return;
        documentoRepository.buscarPendientes(
                EnumSet.of(EstadoDocumentoElectronico.PENDIENTE, EstadoDocumentoElectronico.ERROR,
                        EstadoDocumentoElectronico.CONFIGURACION_PENDIENTE), LocalDateTime.now())
                .stream().limit(20).map(DocumentoElectronico::getIdDocumento).toList()
                .forEach(this::procesar);
    }

    @Transactional(readOnly = true)
    public List<DocumentoElectronicoDTO> listar() {
        return documentoRepository.findAllByOrderByFechaEmisionDesc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public String obtenerXml(Integer id) {
        DocumentoElectronico d = obtener(id);
        if (d.getXml() == null) throw new BusinessException("El XML aún no ha sido generado.");
        return d.getXml();
    }

    @Transactional(readOnly = true)
    public byte[] obtenerCdr(Integer id) {
        DocumentoElectronico d = obtener(id);
        if (d.getCdr() == null) throw new BusinessException("El documento aún no tiene CDR.");
        return d.getCdr();
    }

    private DocumentoElectronico obtener(Integer id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento electrónico no encontrado."));
    }

    private EstadoDocumentoElectronico estadoInicial() {
        return properties.configuracionCompleta() ? EstadoDocumentoElectronico.PENDIENTE
                : EstadoDocumentoElectronico.CONFIGURACION_PENDIENTE;
    }

    private String codigoSunat(DocumentoElectronico d) {
        return switch (d.getTipoDocumento()) { case FACTURA -> "01"; case BOLETA -> "03"; case NOTA_CREDITO -> "07"; };
    }

    private String sha256(String contenido) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(contenido.getBytes(StandardCharsets.UTF_8)));
    }

    private String recortar(String texto, int max) {
        return texto == null ? null : texto.length() <= max ? texto : texto.substring(0, max);
    }

    private DocumentoElectronicoDTO map(DocumentoElectronico d) {
        return new DocumentoElectronicoDTO(d.getIdDocumento(),
                d.getComprobante() == null ? null : d.getComprobante().getIdComprobante(),
                d.getDevolucionVenta() == null ? null : d.getDevolucionVenta().getIdDevolucionVenta(),
                d.getTipoDocumento().name(), d.getSerie(), d.getCorrelativo(), d.getFechaEmision(),
                d.getEstado().name(), d.getHashXml(), d.getCodigoRespuesta(), d.getDescripcionRespuesta(),
                d.getIntentos(), d.getUltimoIntento(), d.getSiguienteIntento());
    }
}
