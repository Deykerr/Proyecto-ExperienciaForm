package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.DetalleMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Integer> {
    List<DetalleMovimiento> findByMovimientoIdMovimiento(Integer idMovimiento);
    List<DetalleMovimiento> findByLoteProductoIdProducto(Integer idProducto);
}
