package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Producto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // Búsqueda rápida por lector de código de barras
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.idProducto = :id")
    Optional<Producto> findByIdWithLock(@org.springframework.data.repository.query.Param("id") Integer id);
    
    // Para alertas en el dashboard: productos que se están agotando
    List<Producto> findByStockActualLessThanEqual(Integer stockMinimo);

    // Para la barra de búsqueda del Punto de Venta (POS)
    Page<Producto> findByNombreContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(String nombre, String codigoBarras, Pageable pageable);
}
