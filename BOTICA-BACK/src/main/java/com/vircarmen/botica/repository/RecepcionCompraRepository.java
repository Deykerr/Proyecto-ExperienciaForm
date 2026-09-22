package com.vircarmen.botica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vircarmen.botica.entity.RecepcionCompra;

public interface RecepcionCompraRepository extends JpaRepository<RecepcionCompra, Integer> {
    Optional<RecepcionCompra> findByIdempotencyKey(String key);
    List<RecepcionCompra> findByCompraIdCompraOrderByFechaDesc(Integer compraId);
}
