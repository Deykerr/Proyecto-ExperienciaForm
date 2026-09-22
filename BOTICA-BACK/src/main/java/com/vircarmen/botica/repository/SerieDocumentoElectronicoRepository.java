package com.vircarmen.botica.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import com.vircarmen.botica.entity.SerieDocumentoElectronico;
import com.vircarmen.botica.entity.TipoDocumentoElectronico;
import jakarta.persistence.LockModeType;

public interface SerieDocumentoElectronicoRepository extends JpaRepository<SerieDocumentoElectronico, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SerieDocumentoElectronico> findByTipoDocumentoAndSerie(TipoDocumentoElectronico tipo, String serie);
}
