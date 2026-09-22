package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
    Optional<DetalleVenta> findByIdDetalleVentaAndVentaIdVenta(Integer idDetalle, Integer idVenta);

    @Query("""
            SELECT COALESCE(SUM(d.cantidad), 0)
            FROM DevolucionVentaDetalle d
            WHERE d.detalleVenta.idDetalleVenta = :detalleId
              AND d.devolucionVenta.estado = com.vircarmen.botica.entity.EstadoDevolucion.CONFIRMADA
            """)
    Long sumCantidadDevuelta(@Param("detalleId") Integer detalleId);
}
