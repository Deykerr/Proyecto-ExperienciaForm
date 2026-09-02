package com.vircarmen.botica.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vircarmen.botica.entity.Venta;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    // Para reportes diarios de caja
    List<Venta> findByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Para buscar facturas específicas rápidamente
    Optional<Venta> findByComprobanteSerieAndComprobanteCorrelativo(String serie, String correlativo);

    // Método añadido: buscar ventas por cliente
    List<Venta> findByClienteIdCliente(Integer idCliente);
}
