package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.CajaSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface CajaSesionRepository extends JpaRepository<CajaSesion, Integer> {
    Optional<CajaSesion> findByUsuarioIdUsuarioAndEstado(Integer idUsuario, CajaSesion.EstadoCaja estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CajaSesion c WHERE c.idCajaSesion = :id")
    Optional<CajaSesion> findByIdWithLock(@Param("id") Integer id);
}
