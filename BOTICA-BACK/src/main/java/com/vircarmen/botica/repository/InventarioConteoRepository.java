package com.vircarmen.botica.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vircarmen.botica.entity.InventarioConteo;

import java.util.List;

public interface InventarioConteoRepository extends JpaRepository<InventarioConteo, Integer> {
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.lote", "detalles.lote.producto"})
    List<InventarioConteo> findAllByOrderByFechaFinalizacionDesc();
}
