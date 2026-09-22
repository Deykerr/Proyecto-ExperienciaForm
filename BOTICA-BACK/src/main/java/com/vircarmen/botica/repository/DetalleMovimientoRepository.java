package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.DetalleMovimiento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Integer> {
    List<DetalleMovimiento> findByMovimientoIdMovimiento(Integer idMovimiento);
    List<DetalleMovimiento> findByLoteProductoIdProducto(Integer idProducto);

    @Query(value = """
            SELECT d FROM DetalleMovimiento d
            JOIN FETCH d.movimiento m
            JOIN FETCH m.usuario
            JOIN FETCH d.producto p
            LEFT JOIN FETCH d.lote l
            WHERE (:productoId IS NULL OR p.idProducto = :productoId)
              AND (:loteId IS NULL OR l.idLote = :loteId)
            ORDER BY m.fechaMovimiento DESC, d.idDetalle DESC
            """,
            countQuery = """
            SELECT COUNT(d) FROM DetalleMovimiento d
            WHERE (:productoId IS NULL OR d.producto.idProducto = :productoId)
              AND (:loteId IS NULL OR d.lote.idLote = :loteId)
            """)
    Page<DetalleMovimiento> buscarKardex(
            @Param("productoId") Integer productoId,
            @Param("loteId") Integer loteId,
            Pageable pageable);
}
