package com.vircarmen.botica.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.vircarmen.botica.entity.EstadoResumenDiarioSunat;
import com.vircarmen.botica.entity.ResumenDiarioSunat;

public interface ResumenDiarioSunatRepository extends JpaRepository<ResumenDiarioSunat, Integer> {
    List<ResumenDiarioSunat> findAllByOrderByFechaGeneracionDesc();

    @Query("""
            SELECT r FROM ResumenDiarioSunat r
            WHERE r.estado IN :estados
              AND (r.siguienteIntento IS NULL OR r.siguienteIntento <= :ahora)
            ORDER BY r.fechaGeneracion ASC
            """)
    List<ResumenDiarioSunat> buscarPendientes(
            @Param("estados") Collection<EstadoResumenDiarioSunat> estados,
            @Param("ahora") LocalDateTime ahora);
}
