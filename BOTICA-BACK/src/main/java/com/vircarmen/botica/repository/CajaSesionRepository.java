package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.CajaSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CajaSesionRepository extends JpaRepository<CajaSesion, Integer> {
    Optional<CajaSesion> findByUsuarioIdUsuarioAndEstado(Integer idUsuario, CajaSesion.EstadoCaja estado);
}
