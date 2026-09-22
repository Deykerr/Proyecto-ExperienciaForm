package com.vircarmen.botica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.vircarmen.botica.entity.Receta;
import jakarta.persistence.LockModeType;

public interface RecetaRepository extends JpaRepository<Receta, Integer> {
    Optional<Receta> findByNumeroIgnoreCase(String numero);
    List<Receta> findAllByOrderByFechaRegistroDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Receta r WHERE r.idReceta = :id")
    Optional<Receta> findByIdWithLock(@Param("id") Integer id);
}
