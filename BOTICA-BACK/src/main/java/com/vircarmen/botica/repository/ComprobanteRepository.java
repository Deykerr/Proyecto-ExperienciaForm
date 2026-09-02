package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Integer> {
}
