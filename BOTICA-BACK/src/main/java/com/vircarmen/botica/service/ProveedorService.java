package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.ProveedorDTO;
import com.vircarmen.botica.dto.ProveedorRequest;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.Proveedor;
import com.vircarmen.botica.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public Page<ProveedorDTO> listarProveedores(Pageable pageable) {
        return proveedorRepository.findAll(pageable).map(this::mapToDTO);
    }

    public ProveedorDTO obtenerProveedor(Integer id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        return mapToDTO(proveedor);
    }

    @Transactional
    public ProveedorDTO guardarProveedor(ProveedorRequest request) {
        if (proveedorRepository.existsByRuc(request.ruc())) {
            throw new RuntimeException("RUC ya registrado");
        }
        if (proveedorRepository.existsByRazonSocial(request.razonSocial())) {
            throw new RuntimeException("Razón Social ya registrada");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setRuc(request.ruc());
        proveedor.setRazonSocial(request.razonSocial());
        proveedor.setTelefono(request.telefono());
        proveedor.setCorreo(request.correo());
        proveedor.setDireccion(request.direccion());
        proveedor.setEstado(EstadoGeneral.A);

        return mapToDTO(proveedorRepository.save(proveedor));
    }

    @Transactional
    public ProveedorDTO actualizarProveedor(Integer id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        proveedor.setRuc(request.ruc());
        proveedor.setRazonSocial(request.razonSocial());
        proveedor.setTelefono(request.telefono());
        proveedor.setCorreo(request.correo());
        proveedor.setDireccion(request.direccion());

        return mapToDTO(proveedorRepository.save(proveedor));
    }

    @Transactional
    public ProveedorDTO cambiarEstado(Integer id, EstadoGeneral estado) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        proveedor.setEstado(estado);
        return mapToDTO(proveedorRepository.save(proveedor));
    }

    private ProveedorDTO mapToDTO(Proveedor p) {
        return new ProveedorDTO(
                p.getIdProveedor(),
                p.getRuc(),
                p.getRazonSocial(),
                p.getTelefono(),
                p.getCorreo(),
                p.getDireccion(),
                p.getEstado().name()
        );
    }
}
