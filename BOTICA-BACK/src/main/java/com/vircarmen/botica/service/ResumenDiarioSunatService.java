package com.vircarmen.botica.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vircarmen.botica.config.SunatProperties;
import com.vircarmen.botica.dto.ResumenDiarioSunatDTO;
import com.vircarmen.botica.entity.DocumentoElectronico;
import com.vircarmen.botica.entity.EstadoDocumentoElectronico;
import com.vircarmen.botica.entity.EstadoResumenDiarioSunat;
import com.vircarmen.botica.entity.ResumenDiarioSunat;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.DocumentoElectronicoRepository;
import com.vircarmen.botica.repository.ResumenDiarioSunatRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumenDiarioSunatService {
    private final ResumenDiarioSunatRepository resumenRepository;
    private final DocumentoElectronicoRepository documentoRepository;
    private final SunatProperties properties;
    private final SunatResumenDiarioGenerator generator;
    private final SunatXmlSigner signer;
    private final SunatSoapClient soapClient;

    @Transactional
    public ResumenDiarioSunatDTO crearYEnviar() {
        if (!properties.configuracionCompleta()) {
            throw new BusinessException("La configuración SUNAT aún no está completa.");
        }
        List<DocumentoElectronico> candidatos = documentoRepository.buscarFirmadosParaResumen();
        if (candidatos.isEmpty()) {
            throw new BusinessException("No hay boletas o notas firmadas pendientes de Resumen Diario.");
        }
        LocalDate fechaReferencia = candidatos.getFirst().getFechaEmision().toLocalDate();
        List<DocumentoElectronico> documentos = candidatos.stream()
                .filter(d -> d.getFechaEmision().toLocalDate().equals(fechaReferencia)).toList();

        ResumenDiarioSunat resumen = new ResumenDiarioSunat();
        resumen.setFechaReferencia(fechaReferencia);
        resumen.setFechaGeneracion(LocalDateTime.now());
        resumen.setEstado(EstadoResumenDiarioSunat.PENDIENTE);
        resumen = resumenRepository.saveAndFlush(resumen);
        if (resumen.getIdResumen() > 99999) {
            throw new BusinessException("Se agotó el correlativo permitido para el Resumen Diario.");
        }
        resumen.setIdentificador("RC-" + resumen.getFechaGeneracion().toLocalDate()
                .format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + resumen.getIdResumen());
        for (DocumentoElectronico documento : documentos) {
            documento.setResumenDiario(resumen);
            resumen.getDocumentos().add(documento);
        }
        documentoRepository.saveAll(documentos);
        resumenRepository.saveAndFlush(resumen);
        return procesarInterno(resumen);
    }

    @Transactional
    public ResumenDiarioSunatDTO procesar(Integer id) {
        ResumenDiarioSunat resumen = resumenRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Resumen Diario SUNAT no encontrado."));
        return procesarInterno(resumen);
    }

    private ResumenDiarioSunatDTO procesarInterno(ResumenDiarioSunat resumen) {
        if (!properties.configuracionCompleta()) {
            throw new BusinessException("La configuración SUNAT aún no está completa.");
        }
        if (resumen.getEstado() == EstadoResumenDiarioSunat.ACEPTADO
                || resumen.getEstado() == EstadoResumenDiarioSunat.OBSERVADO
                || resumen.getEstado() == EstadoResumenDiarioSunat.RECHAZADO) {
            return map(resumen);
        }
        resumen.setIntentos(resumen.getIntentos() + 1);
        resumen.setUltimoIntento(LocalDateTime.now());
        resumen.setSiguienteIntento(null);
        try {
            if (resumen.getTicket() == null) {
                if (resumen.getXml() == null || resumen.getEstado() == EstadoResumenDiarioSunat.PENDIENTE) {
                    String xml = generator.generar(resumen, resumen.getDocumentos());
                    String firmado = signer.firmar(xml);
                    resumen.setXml(firmado);
                    resumen.setHashXml(sha256(firmado));
                    resumen.setEstado(EstadoResumenDiarioSunat.FIRMADO);
                    resumenRepository.saveAndFlush(resumen);
                }
                String ticket = soapClient.enviarResumen(properties.getRuc() + "-" + resumen.getIdentificador(),
                        resumen.getXml());
                resumen.setTicket(ticket);
                resumen.setEstado(EstadoResumenDiarioSunat.ENVIADO);
                resumen.setDescripcionRespuesta("Resumen recibido por SUNAT; pendiente de procesamiento.");
                resumen.setSiguienteIntento(LocalDateTime.now().plusMinutes(1));
                marcarDocumentos(resumen, EstadoDocumentoElectronico.ENVIADO, null, null, null);
            } else {
                consultarTicket(resumen);
            }
        } catch (Exception ex) {
            resumen.setEstado(EstadoResumenDiarioSunat.ERROR);
            resumen.setDescripcionRespuesta(recortar(ex.getMessage() == null
                    ? ex.getClass().getSimpleName() : ex.getMessage(), 1000));
            long minutos = Math.min(60, 1L << Math.min(resumen.getIntentos(), 6));
            resumen.setSiguienteIntento(LocalDateTime.now().plusMinutes(minutos));
        }
        return map(resumenRepository.save(resumen));
    }

    private void consultarTicket(ResumenDiarioSunat resumen) throws Exception {
        SunatSoapClient.EstadoTicket estado = soapClient.consultarResumen(resumen.getTicket());
        if (!estado.terminado()) {
            resumen.setEstado(EstadoResumenDiarioSunat.ENVIADO);
            resumen.setCodigoRespuesta(estado.estadoTicket());
            resumen.setDescripcionRespuesta(estado.descripcion());
            resumen.setSiguienteIntento(LocalDateTime.now().plusMinutes(1));
            return;
        }
        resumen.setCdr(estado.cdr());
        resumen.setCodigoRespuesta(estado.codigo());
        resumen.setDescripcionRespuesta(recortar(estado.descripcion(), 1000));
        EstadoResumenDiarioSunat estadoFinal = estado.aceptada()
                ? estado.observada() ? EstadoResumenDiarioSunat.OBSERVADO : EstadoResumenDiarioSunat.ACEPTADO
                : EstadoResumenDiarioSunat.RECHAZADO;
        resumen.setEstado(estadoFinal);
        EstadoDocumentoElectronico estadoDocumento = estadoFinal == EstadoResumenDiarioSunat.ACEPTADO
                ? EstadoDocumentoElectronico.ACEPTADO
                : estadoFinal == EstadoResumenDiarioSunat.OBSERVADO
                    ? EstadoDocumentoElectronico.OBSERVADO : EstadoDocumentoElectronico.RECHAZADO;
        marcarDocumentos(resumen, estadoDocumento, estado.cdr(), estado.codigo(), estado.descripcion());
    }

    private void marcarDocumentos(ResumenDiarioSunat resumen, EstadoDocumentoElectronico estado,
            byte[] cdr, String codigo, String descripcion) {
        resumen.getDocumentos().forEach(documento -> {
            documento.setEstado(estado);
            documento.setCdr(cdr);
            documento.setCodigoRespuesta(codigo);
            documento.setDescripcionRespuesta(recortar(descripcion, 1000));
        });
        documentoRepository.saveAll(resumen.getDocumentos());
    }

    @Scheduled(fixedDelayString = "${app.sunat.resumen-procesamiento-ms:60000}", initialDelay = 15000)
    @Transactional
    public void procesarPendientes() {
        if (!properties.configuracionCompleta()) return;
        resumenRepository.buscarPendientes(EnumSet.of(EstadoResumenDiarioSunat.PENDIENTE,
                EstadoResumenDiarioSunat.FIRMADO, EstadoResumenDiarioSunat.ENVIADO,
                EstadoResumenDiarioSunat.ERROR), LocalDateTime.now()).stream().limit(10)
                .map(ResumenDiarioSunat::getIdResumen).toList().forEach(this::procesar);
        if (!documentoRepository.buscarFirmadosParaResumen().isEmpty()) {
            crearYEnviar();
        }
    }

    @Transactional(readOnly = true)
    public List<ResumenDiarioSunatDTO> listar() {
        return resumenRepository.findAllByOrderByFechaGeneracionDesc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public String obtenerXml(Integer id) {
        ResumenDiarioSunat r = obtener(id);
        if (r.getXml() == null) throw new BusinessException("El XML del resumen aún no fue generado.");
        return r.getXml();
    }

    @Transactional(readOnly = true)
    public byte[] obtenerCdr(Integer id) {
        ResumenDiarioSunat r = obtener(id);
        if (r.getCdr() == null) throw new BusinessException("El resumen aún no tiene CDR.");
        return r.getCdr();
    }

    private ResumenDiarioSunat obtener(Integer id) {
        return resumenRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Resumen Diario SUNAT no encontrado."));
    }

    private String sha256(String contenido) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(contenido.getBytes(StandardCharsets.UTF_8)));
    }

    private String recortar(String texto, int max) {
        return texto == null ? null : texto.length() <= max ? texto : texto.substring(0, max);
    }

    private ResumenDiarioSunatDTO map(ResumenDiarioSunat r) {
        return new ResumenDiarioSunatDTO(r.getIdResumen(), r.getIdentificador(), r.getFechaReferencia(),
                r.getFechaGeneracion(), r.getEstado().name(), r.getHashXml(), r.getTicket(),
                r.getCodigoRespuesta(), r.getDescripcionRespuesta(), r.getIntentos(), r.getDocumentos().size());
    }
}
