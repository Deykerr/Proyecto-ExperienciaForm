package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Integer> {
    Optional<MovimientoCaja> findByIdempotencyKey(String idempotencyKey);
    List<MovimientoCaja> findByCajaSesionIdCajaSesionOrderByFechaAsc(Integer cajaId);
}
