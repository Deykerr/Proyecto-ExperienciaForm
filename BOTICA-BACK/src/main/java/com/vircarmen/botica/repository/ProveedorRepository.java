package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    boolean existsByRuc(String ruc);
    boolean existsByRazonSocial(String razonSocial);
}
