package com.vircarmen.botica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vircarmen.botica.entity.DevolucionVenta;

public interface DevolucionVentaRepository extends JpaRepository<DevolucionVenta, Integer> {
    Optional<DevolucionVenta> findByIdempotencyKey(String key);
    List<DevolucionVenta> findByVentaIdVentaOrderByFechaDesc(Integer ventaId);
}
