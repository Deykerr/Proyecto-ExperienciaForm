package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, Integer> {
    boolean existsByNombre(String nombre);
}
