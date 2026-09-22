package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {
    Optional<Compra> findByIdempotencyKey(String idempotencyKey);
    boolean existsByProveedorIdProveedorAndDocumentoIgnoreCase(Integer proveedorId, String documento);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compra c WHERE c.idCompra = :id")
    Optional<Compra> findByIdWithLock(@Param("id") Integer id);
}
