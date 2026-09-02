package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {
}
