package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {
    
    @Query("SELECT m FROM Movimiento m JOIN FETCH m.usuario LEFT JOIN FETCH m.proveedor ORDER BY m.fechaMovimiento DESC")
    List<Movimiento> findAllWithDetails();

    Optional<Movimiento> findByIdempotencyKey(String idempotencyKey);
}
