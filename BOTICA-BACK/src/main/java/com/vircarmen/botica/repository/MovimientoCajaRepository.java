package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Integer> {
}
