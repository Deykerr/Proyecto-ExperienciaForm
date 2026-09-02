package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.LaboratorioDTO;
import com.vircarmen.botica.dto.LaboratorioRequest;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.Laboratorio;
import com.vircarmen.botica.repository.LaboratorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LaboratorioService {

    private final LaboratorioRepository laboratorioRepository;

    public Page<LaboratorioDTO> listarLaboratorios(Pageable pageable) {
        return laboratorioRepository.findAll(pageable).map(this::mapToDTO);
    }

    public LaboratorioDTO obtenerLaboratorio(Integer id) {
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado"));
        return mapToDTO(laboratorio);
    }

    @Transactional
    public LaboratorioDTO guardarLaboratorio(LaboratorioRequest request) {
        if (laboratorioRepository.existsByNombre(request.nombre())) {
            throw new RuntimeException("Ya existe un laboratorio con ese nombre");
        }
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.setNombre(request.nombre());
        laboratorio.setDescripcion(request.descripcion());
        laboratorio.setEstado(EstadoGeneral.A);
        return mapToDTO(laboratorioRepository.save(laboratorio));
    }

    @Transactional
    public LaboratorioDTO actualizarLaboratorio(Integer id, LaboratorioRequest request) {
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado"));
        laboratorio.setNombre(request.nombre());
        laboratorio.setDescripcion(request.descripcion());
        return mapToDTO(laboratorioRepository.save(laboratorio));
    }

    @Transactional
    public LaboratorioDTO cambiarEstado(Integer id, EstadoGeneral estado) {
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratorio no encontrado"));
        laboratorio.setEstado(estado);
        return mapToDTO(laboratorioRepository.save(laboratorio));
    }

    private LaboratorioDTO mapToDTO(Laboratorio l) {
        return new LaboratorioDTO(
                l.getIdLaboratorio(),
                l.getNombre(),
                l.getDescripcion(),
                l.getEstado().name()
        );
    }
}
