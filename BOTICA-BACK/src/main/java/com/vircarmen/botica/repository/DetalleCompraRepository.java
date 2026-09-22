package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Integer> {
    Optional<DetalleCompra> findByIdDetalleCompraAndCompraIdCompra(Integer idDetalle, Integer idCompra);

    @Query("""
            SELECT COALESCE(SUM(d.cantidad), 0)
            FROM DevolucionProveedorDetalle d
            WHERE d.detalleCompra.idDetalleCompra = :detalleId
              AND d.lote.idLote = :loteId
              AND d.devolucionProveedor.estado = com.vircarmen.botica.entity.EstadoDevolucionProveedor.CONFIRMADA
            """)
    Long sumCantidadDevueltaPorLote(@Param("detalleId") Integer detalleId, @Param("loteId") Integer loteId);

    @Query("""
            SELECT COALESCE(SUM(r.cantidad), 0)
            FROM RecepcionCompraDetalle r
            WHERE r.detalleCompra.idDetalleCompra = :detalleId AND r.lote.idLote = :loteId
              AND r.recepcion.estado = com.vircarmen.botica.entity.EstadoRecepcionCompra.CONFIRMADA
            """)
    Long sumCantidadRecibidaPorLote(@Param("detalleId") Integer detalleId, @Param("loteId") Integer loteId);
}
