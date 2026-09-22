package com.vircarmen.botica.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vircarmen.botica.dto.RecetaDTO;
import com.vircarmen.botica.dto.RecetaRequest;
import com.vircarmen.botica.entity.Cliente;
import com.vircarmen.botica.entity.CondicionVenta;
import com.vircarmen.botica.entity.EstadoReceta;
import com.vircarmen.botica.entity.Producto;
import com.vircarmen.botica.entity.Receta;
import com.vircarmen.botica.entity.RecetaDetalle;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.ClienteRepository;
import com.vircarmen.botica.repository.ProductoRepository;
import com.vircarmen.botica.repository.RecetaRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecetaService {
    private final RecetaRepository recetaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public RecetaDTO registrar(RecetaRequest request) {
        if (recetaRepository.findByNumeroIgnoreCase(request.numero().trim()).isPresent()) {
            throw new BusinessException("El número de receta ya está registrado.");
        }
        if (request.fechaEmision().isAfter(LocalDate.now())) {
            throw new BusinessException("La fecha de emisión de la receta no puede ser futura.");
        }
        if (request.fechaVencimiento() != null
                && request.fechaVencimiento().isBefore(request.fechaEmision())) {
            throw new BusinessException("La fecha de vencimiento no puede ser anterior a la emisión.");
        }

        CondicionVenta condicion = parseCondicion(request.condicionVenta());
        if (condicion == CondicionVenta.SIN_RECETA_MEDICA) {
            throw new BusinessException("Una receta debe corresponder a una condición de venta con receta.");
        }
        Set<Integer> productos = new HashSet<>();
        request.detalles().forEach(item -> {
            if (!productos.add(item.idProducto())) {
                throw new BusinessException("Un producto no puede repetirse en la receta.");
            }
        });

        Cliente cliente = request.idCliente() == null ? null : clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new BusinessException("Cliente no encontrado."));
        Usuario usuario = usuarioAutenticado();
        Receta receta = new Receta();
        receta.setNumero(request.numero().trim().toUpperCase(Locale.ROOT));
        receta.setCliente(cliente);
        receta.setPacienteNombre(request.pacienteNombre().trim());
        receta.setPacienteDocumento(normalizar(request.pacienteDocumento()));
        receta.setMedicoNombre(request.medicoNombre().trim());
        receta.setMedicoColegiatura(request.medicoColegiatura().trim().toUpperCase(Locale.ROOT));
        receta.setFechaEmision(request.fechaEmision());
        receta.setFechaVencimiento(request.fechaVencimiento());
        receta.setCondicionVenta(condicion);
        receta.setEstado(estaVencida(receta) ? EstadoReceta.VENCIDA : EstadoReceta.DISPONIBLE);
        receta.setObservaciones(normalizar(request.observaciones()));
        receta.setUsuario(usuario);
        receta.setFechaRegistro(LocalDateTime.now());

        for (RecetaRequest.Item item : request.detalles()) {
            Producto producto = productoRepository.findById(item.idProducto())
                    .orElseThrow(() -> new BusinessException("Producto no encontrado: " + item.idProducto()));
            if (!producto.getCondicionVenta().requiereReceta()) {
                throw new BusinessException("El producto " + producto.getNombre() + " está registrado como venta sin receta.");
            }
            if (producto.getCondicionVenta() == CondicionVenta.CON_RECETA_MEDICA_RETENIDA
                    && condicion != CondicionVenta.CON_RECETA_MEDICA_RETENIDA) {
                throw new BusinessException("El producto " + producto.getNombre() + " requiere receta médica retenida.");
            }
            RecetaDetalle detalle = new RecetaDetalle();
            detalle.setReceta(receta);
            detalle.setProducto(producto);
            detalle.setCantidadAutorizada(item.cantidadAutorizada());
            detalle.setCantidadDispensada(0);
            detalle.setIndicaciones(normalizar(item.indicaciones()));
            receta.getDetalles().add(detalle);
        }
        return map(recetaRepository.save(receta));
    }

    @Transactional(readOnly = true)
    public List<RecetaDTO> listar() {
        return recetaRepository.findAllByOrderByFechaRegistroDesc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public RecetaDTO obtener(Integer id) {
        return map(recetaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Receta no encontrada.")));
    }

    @Transactional
    public RecetaDTO anular(Integer id, String motivo) {
        Receta receta = recetaRepository.findByIdWithLock(id)
                .orElseThrow(() -> new BusinessException("Receta no encontrada."));
        if (receta.getDetalles().stream().anyMatch(d -> d.getCantidadDispensada() > 0)) {
            throw new BusinessException("No se puede anular una receta con dispensaciones registradas.");
        }
        receta.setEstado(EstadoReceta.ANULADA);
        receta.setObservaciones((receta.getObservaciones() == null ? "" : receta.getObservaciones() + " | ")
                + "ANULADA: " + motivo.trim());
        return map(recetaRepository.save(receta));
    }

    /** Valida y consume las cantidades prescritas dentro de la transacción de venta. */
    public Receta validarYDispensar(Integer idReceta, Cliente cliente, Map<Integer, Integer> cantidades) {
        if (cantidades.isEmpty()) {
            return null;
        }
        if (idReceta == null) {
            throw new BusinessException("La venta contiene productos que requieren una receta registrada.");
        }
        Receta receta = recetaRepository.findByIdWithLock(idReceta)
                .orElseThrow(() -> new BusinessException("Receta no encontrada."));
        actualizarVencimiento(receta);
        if (receta.getEstado() != EstadoReceta.DISPONIBLE) {
            throw new BusinessException("La receta no está disponible para dispensación: " + receta.getEstado());
        }
        if (receta.getCliente() != null
                && (cliente == null || !receta.getCliente().getIdCliente().equals(cliente.getIdCliente()))) {
            throw new BusinessException("La receta pertenece a otro cliente.");
        }

        Map<Integer, RecetaDetalle> porProducto = new HashMap<>();
        receta.getDetalles().forEach(d -> porProducto.put(d.getProducto().getIdProducto(), d));
        for (Map.Entry<Integer, Integer> item : cantidades.entrySet()) {
            RecetaDetalle detalle = porProducto.get(item.getKey());
            if (detalle == null) {
                throw new BusinessException("La receta no autoriza uno de los productos solicitados.");
            }
            int disponible = detalle.getCantidadAutorizada() - detalle.getCantidadDispensada();
            if (item.getValue() > disponible) {
                throw new BusinessException("La cantidad solicitada supera el saldo autorizado de la receta para "
                        + detalle.getProducto().getNombre() + ". Disponible: " + disponible);
            }
            if (detalle.getProducto().getCondicionVenta() == CondicionVenta.CON_RECETA_MEDICA_RETENIDA
                    && receta.getCondicionVenta() != CondicionVenta.CON_RECETA_MEDICA_RETENIDA) {
                throw new BusinessException("La condición de la receta no permite dispensar "
                        + detalle.getProducto().getNombre() + ".");
            }
            detalle.setCantidadDispensada(detalle.getCantidadDispensada() + item.getValue());
        }
        if (receta.getCondicionVenta() == CondicionVenta.CON_RECETA_MEDICA_RETENIDA
                && receta.getRetenidaEn() == null) {
            receta.setRetenidaEn(LocalDateTime.now());
        }
        if (receta.getDetalles().stream()
                .allMatch(d -> d.getCantidadDispensada().equals(d.getCantidadAutorizada()))) {
            receta.setEstado(EstadoReceta.AGOTADA);
        }
        return recetaRepository.save(receta);
    }

    public void revertirDispensacion(Receta receta, Map<Integer, Integer> cantidades) {
        if (receta == null || cantidades.isEmpty()) return;
        Receta bloqueada = recetaRepository.findByIdWithLock(receta.getIdReceta())
                .orElseThrow(() -> new BusinessException("Receta no encontrada."));
        Map<Integer, RecetaDetalle> porProducto = new HashMap<>();
        bloqueada.getDetalles().forEach(d -> porProducto.put(d.getProducto().getIdProducto(), d));
        cantidades.forEach((productoId, cantidad) -> {
            RecetaDetalle detalle = porProducto.get(productoId);
            if (detalle == null || detalle.getCantidadDispensada() < cantidad) {
                throw new BusinessException("No se pudo revertir la dispensación de la receta.");
            }
            detalle.setCantidadDispensada(detalle.getCantidadDispensada() - cantidad);
        });
        actualizarVencimiento(bloqueada);
        if (bloqueada.getEstado() == EstadoReceta.AGOTADA) {
            bloqueada.setEstado(EstadoReceta.DISPONIBLE);
        }
        recetaRepository.save(bloqueada);
    }

    private void actualizarVencimiento(Receta receta) {
        if (receta.getEstado() != EstadoReceta.ANULADA && estaVencida(receta)) {
            receta.setEstado(EstadoReceta.VENCIDA);
        }
    }

    private boolean estaVencida(Receta receta) {
        return receta.getFechaVencimiento() != null
                && receta.getFechaVencimiento().isBefore(LocalDate.now());
    }

    private RecetaDTO map(Receta receta) {
        actualizarVencimiento(receta);
        return new RecetaDTO(
                receta.getIdReceta(), receta.getNumero(),
                receta.getCliente() == null ? null : receta.getCliente().getIdCliente(),
                receta.getPacienteNombre(), receta.getPacienteDocumento(), receta.getMedicoNombre(),
                receta.getMedicoColegiatura(), receta.getFechaEmision(), receta.getFechaVencimiento(),
                receta.getCondicionVenta().name(), receta.getCondicionVenta().getCodigoDigemid(),
                receta.getEstado().name(), receta.getRetenidaEn(), receta.getObservaciones(),
                receta.getDetalles().stream().map(d -> new RecetaDTO.Item(
                        d.getIdRecetaDetalle(), d.getProducto().getIdProducto(), d.getProducto().getNombre(),
                        d.getCantidadAutorizada(), d.getCantidadDispensada(),
                        d.getCantidadAutorizada() - d.getCantidadDispensada(), d.getIndicaciones())).toList());
    }

    private CondicionVenta parseCondicion(String valor) {
        try {
            return CondicionVenta.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException("Condición de venta no válida.");
        }
    }

    private Usuario usuarioAutenticado() {
        return usuarioRepository.findById(SecurityUtils.getUsuarioAutenticadoId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
