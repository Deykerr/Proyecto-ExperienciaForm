package com.vircarmen.botica.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.vircarmen.botica.entity.SerieComprobante;
import com.vircarmen.botica.entity.TipoComprobante;

import jakarta.persistence.LockModeType;

public interface SerieComprobanteRepository extends JpaRepository<SerieComprobante, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SerieComprobante> findByTipoComprobanteAndSerie(TipoComprobante tipoComprobante, String serie);
}
