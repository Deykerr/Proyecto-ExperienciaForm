package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    boolean existsByNombre(String nombre);
}
