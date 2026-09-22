package com.vircarmen.botica.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vircarmen.botica.entity.ArqueoCaja;

public interface ArqueoCajaRepository extends JpaRepository<ArqueoCaja, Integer> {
    Optional<ArqueoCaja> findByCajaSesionIdCajaSesion(Integer cajaId);
}
