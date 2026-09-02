package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Integer> {
    
    @Query("SELECT l FROM Lote l WHERE l.producto.idProducto = :productoId AND l.stockActual > 0 ORDER BY l.fechaVencimiento ASC")
    List<Lote> findLotesDisponiblesByProductoOrderByFechaVencimientoAsc(@Param("productoId") Integer productoId);
    
    @Query("SELECT l FROM Lote l WHERE l.stockActual > 0 ORDER BY l.fechaVencimiento ASC")
    List<Lote> findLotesProximosAVencer();
}

