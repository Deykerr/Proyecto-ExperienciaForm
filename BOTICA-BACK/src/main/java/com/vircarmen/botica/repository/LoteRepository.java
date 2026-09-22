package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoteRepository extends JpaRepository<Lote, Integer> {

    @Query("SELECT l FROM Lote l JOIN FETCH l.producto p ORDER BY p.nombre ASC, l.fechaVencimiento ASC")
    List<Lote> findAllWithProducto();
    
    @Query("SELECT l FROM Lote l WHERE l.producto.idProducto = :productoId AND l.stockActual > 0 ORDER BY l.fechaVencimiento ASC")
    List<Lote> findLotesDisponiblesByProductoOrderByFechaVencimientoAsc(@Param("productoId") Integer productoId);
    
    @Query("SELECT l FROM Lote l JOIN FETCH l.producto WHERE l.stockActual > 0 ORDER BY l.fechaVencimiento ASC")
    List<Lote> findLotesProximosAVencer();

    @Query("""
            SELECT l FROM Lote l
            JOIN FETCH l.producto p
            WHERE p.estado = com.vircarmen.botica.entity.EstadoGeneral.A
              AND l.stockActual > 0
              AND l.fechaVencimiento <= :fechaLimite
            ORDER BY l.fechaVencimiento ASC, p.nombre ASC, l.codigoLote ASC
            """)
    List<Lote> findLotesConStockHastaFecha(@Param("fechaLimite") LocalDate fechaLimite);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lote l JOIN FETCH l.producto WHERE l.idLote = :id")
    Optional<Lote> findByIdWithLock(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT l FROM Lote l
            WHERE l.producto.idProducto = :productoId
              AND l.estado = com.vircarmen.botica.entity.EstadoLote.DISPONIBLE
              AND l.stockActual > 0
              AND l.fechaVencimiento > :hoy
            ORDER BY l.fechaVencimiento ASC, l.idLote ASC
            """)
    List<Lote> findVendiblesFEFOForUpdate(
            @Param("productoId") Integer productoId,
            @Param("hoy") LocalDate hoy);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Lote> findByProductoIdProductoAndCodigoLote(Integer productoId, String codigoLote);

    List<Lote> findByProductoIdProducto(Integer productoId);

    @Query("SELECT COALESCE(SUM(l.stockActual), 0) FROM Lote l WHERE l.producto.idProducto = :productoId")
    Long sumStockByProductoId(@Param("productoId") Integer productoId);
}

