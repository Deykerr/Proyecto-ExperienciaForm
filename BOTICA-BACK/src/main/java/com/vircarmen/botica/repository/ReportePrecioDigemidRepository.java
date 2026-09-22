package com.vircarmen.botica.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vircarmen.botica.entity.ReportePrecioDigemid;

public interface ReportePrecioDigemidRepository extends JpaRepository<ReportePrecioDigemid, Integer> {
    Optional<ReportePrecioDigemid> findByPeriodo(String periodo);
    List<ReportePrecioDigemid> findAllByOrderByPeriodoDesc();
}
