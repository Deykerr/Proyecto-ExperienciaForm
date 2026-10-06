package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Producto;

import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Producto> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Producto> findById(Integer id);

    @Override
    @EntityGraph(attributePaths = "categoria")
    List<Producto> findAllById(Iterable<Integer> ids);

    // Búsqueda rápida por lector de código de barras
    @EntityGraph(attributePaths = "categoria")
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.idProducto = :id")
    Optional<Producto> findByIdWithLock(@org.springframework.data.repository.query.Param("id") Integer id);
    
    // Compara el stock de cada producto contra su propio mínimo, no contra un valor global.
    @Query("""
            SELECT p FROM Producto p
            WHERE p.estado = com.vircarmen.botica.entity.EstadoGeneral.A
              AND COALESCE(p.stockActual, 0) <= COALESCE(p.stockMinimo, 0)
            ORDER BY COALESCE(p.stockActual, 0) ASC, p.nombre ASC
            """)
    List<Producto> findProductosConStockBajo();

    // Para la barra de búsqueda del Punto de Venta (POS)
    @EntityGraph(attributePaths = "categoria")
    Page<Producto> findByNombreContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(String nombre, String codigoBarras, Pageable pageable);

    @Query("""
            SELECT p.idProducto
            FROM DetalleVenta d
            JOIN d.lote l
            JOIN l.producto p
            WHERE d.venta.fechaEmision >= :desde
              AND d.venta.estado IN (
                  com.vircarmen.botica.entity.EstadoVenta.PAGADA,
                  com.vircarmen.botica.entity.EstadoVenta.DEVUELTA_PARCIAL
              )
              AND p.estado = com.vircarmen.botica.entity.EstadoGeneral.A
              AND COALESCE(p.stockActual, 0) > 0
              AND EXISTS (
                  SELECT 1 FROM Lote loteVenta
                  WHERE loteVenta.producto = p
                    AND loteVenta.estado = com.vircarmen.botica.entity.EstadoLote.DISPONIBLE
                    AND COALESCE(loteVenta.stockActual, 0) > 0
                    AND loteVenta.fechaVencimiento >= :hoy
              )
            GROUP BY p.idProducto, p.nombre
            ORDER BY SUM(d.cantidad) DESC, p.nombre ASC
            """)
    List<Integer> findIdsMasVendidosDesde(
            @Param("desde") LocalDateTime desde,
            @Param("hoy") LocalDate hoy,
            Pageable pageable);

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            SELECT p FROM Producto p
            WHERE p.estado = com.vircarmen.botica.entity.EstadoGeneral.A
              AND COALESCE(p.stockActual, 0) > 0
              AND EXISTS (
                  SELECT 1 FROM Lote loteVenta
                  WHERE loteVenta.producto = p
                    AND loteVenta.estado = com.vircarmen.botica.entity.EstadoLote.DISPONIBLE
                    AND COALESCE(loteVenta.stockActual, 0) > 0
                    AND loteVenta.fechaVencimiento >= :hoy
              )
            ORDER BY p.nombre ASC
            """)
    List<Producto> findDisponiblesParaVenta(@Param("hoy") LocalDate hoy, Pageable pageable);
}
