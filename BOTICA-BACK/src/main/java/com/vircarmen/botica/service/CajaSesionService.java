package com.vircarmen.botica.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.CajaSesionCierreRequest;
import com.vircarmen.botica.dto.CajaSesionDTO;
import com.vircarmen.botica.dto.CajaSesionRequest;
import com.vircarmen.botica.entity.CajaSesion;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.repository.CajaSesionRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CajaSesionService {

    private final CajaSesionRepository cajaSesionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CajaSesionDTO abrirCaja(CajaSesionRequest request) {
        // Obtenemos el ID de forma segura desde el token JWT
        Integer idUsuario = SecurityUtils.getUsuarioAutenticadoId();

        // Verificar que el usuario exista
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Usuario no encontrado"));

        // Verificar que el usuario no tenga ya una caja abierta
        boolean tieneCajaAbierta = cajaSesionRepository.findByUsuarioIdUsuarioAndEstado(idUsuario, CajaSesion.EstadoCaja.ABIERTA).isPresent();
        if (tieneCajaAbierta) {
            throw new com.vircarmen.botica.exception.BusinessException("El usuario ya tiene una caja abierta");
        }

        CajaSesion caja = new CajaSesion();
        caja.setUsuario(usuario);
        caja.setFechaApertura(LocalDateTime.now());
        caja.setMontoInicial(request.montoInicial());
        caja.setEstado(CajaSesion.EstadoCaja.ABIERTA);

        return mapToDTO(cajaSesionRepository.save(caja));
    }

    @Transactional
    public CajaSesionDTO cerrarCaja(Integer idCaja, CajaSesionCierreRequest request) {
        Integer idUsuario = SecurityUtils.getUsuarioAutenticadoId();
        
        CajaSesion caja = cajaSesionRepository.findById(idCaja)
                .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Caja no encontrada"));

        if (!caja.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new com.vircarmen.botica.exception.BusinessException("No tienes permiso para cerrar una caja de otro usuario");
        }

        if (caja.getEstado() == CajaSesion.EstadoCaja.CERRADA) {
            throw new com.vircarmen.botica.exception.BusinessException("Esta caja ya se encuentra cerrada");
        }

        caja.setFechaCierre(LocalDateTime.now());
        caja.setMontoFinal(request.montoFinal());
        caja.setEstado(CajaSesion.EstadoCaja.CERRADA);

        return mapToDTO(cajaSesionRepository.save(caja));
    }

    public CajaSesionDTO obtenerCajaActual(Integer idUsuario) {
    idUsuario = SecurityUtils.getUsuarioAutenticadoId();

    return cajaSesionRepository
            .findByUsuarioIdUsuarioAndEstado(
                    idUsuario,
                    CajaSesion.EstadoCaja.ABIERTA
            )
            .map(this::mapToDTO)
            .orElse(null);
}

    private CajaSesionDTO mapToDTO(CajaSesion c) {
        java.math.BigDecimal ingresosVentas = c.getPagos().stream()
                .map(com.vircarmen.botica.entity.Pago::getMonto)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                
        java.math.BigDecimal ingresosMov = c.getMovimientos().stream()
                .filter(m -> "INGRESO".equals(m.getTipoMovimiento()))
                .map(com.vircarmen.botica.entity.MovimientoCaja::getMonto)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                
        java.math.BigDecimal egresosMov = c.getMovimientos().stream()
                .filter(m -> "EGRESO".equals(m.getTipoMovimiento()))
                .map(com.vircarmen.botica.entity.MovimientoCaja::getMonto)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                
        java.math.BigDecimal totalIngresos = ingresosVentas.add(ingresosMov);
        java.math.BigDecimal saldoCalculado = c.getMontoInicial().add(totalIngresos).subtract(egresosMov);

        return new CajaSesionDTO(
                c.getIdCajaSesion(),
                c.getUsuario().getUsername(), // asumiendo que Usuario tiene getUsername()
                c.getFechaApertura(),
                c.getFechaCierre(),
                c.getMontoInicial(),
                c.getMontoFinal(),
                totalIngresos,
                egresosMov,
                saldoCalculado,
                c.getEstado().name()
        );
    }
}
