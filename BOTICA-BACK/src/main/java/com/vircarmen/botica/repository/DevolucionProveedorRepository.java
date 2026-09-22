package com.vircarmen.botica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vircarmen.botica.entity.DevolucionProveedor;

public interface DevolucionProveedorRepository extends JpaRepository<DevolucionProveedor, Integer> {
    Optional<DevolucionProveedor> findByIdempotencyKey(String key);
    List<DevolucionProveedor> findByCompraIdCompraOrderByFechaDesc(Integer compraId);
}
