package com.vircarmen.botica.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.vircarmen.botica.entity.DocumentoElectronico;
import com.vircarmen.botica.entity.EstadoDocumentoElectronico;

public interface DocumentoElectronicoRepository extends JpaRepository<DocumentoElectronico, Integer> {
    Optional<DocumentoElectronico> findByComprobanteIdComprobante(Integer id);
    Optional<DocumentoElectronico> findByDevolucionVentaIdDevolucionVenta(Integer id);
    List<DocumentoElectronico> findAllByOrderByFechaEmisionDesc();
    @Query("""
            SELECT d FROM DocumentoElectronico d
            WHERE d.estado = com.vircarmen.botica.entity.EstadoDocumentoElectronico.FIRMADO
              AND d.resumenDiario IS NULL
              AND (d.tipoDocumento = com.vircarmen.botica.entity.TipoDocumentoElectronico.BOLETA
                OR (d.tipoDocumento = com.vircarmen.botica.entity.TipoDocumentoElectronico.NOTA_CREDITO
                  AND d.devolucionVenta.venta.comprobante.tipoComprobante = com.vircarmen.botica.entity.TipoComprobante.BOLETA))
            ORDER BY d.fechaEmision ASC
            """)
    List<DocumentoElectronico> buscarFirmadosParaResumen();
    @Query("""
            SELECT d FROM DocumentoElectronico d
            WHERE d.estado IN :estados
              AND (d.siguienteIntento IS NULL OR d.siguienteIntento <= :ahora)
            ORDER BY d.fechaEmision ASC
            """)
    List<DocumentoElectronico> buscarPendientes(@Param("estados") Collection<EstadoDocumentoElectronico> estados,
                                                @Param("ahora") LocalDateTime ahora);
}
