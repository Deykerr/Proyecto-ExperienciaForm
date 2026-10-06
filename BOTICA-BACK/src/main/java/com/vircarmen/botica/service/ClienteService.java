package com.vircarmen.botica.service;

import com.vircarmen.botica.dto.ClienteDTO;
import com.vircarmen.botica.dto.ClienteRequest;
import com.vircarmen.botica.dto.ConsultaDocumentoDTO;
import com.vircarmen.botica.entity.Cliente;
import com.vircarmen.botica.exception.BusinessException;
import com.vircarmen.botica.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ConsultaDocumentoService consultaDocumentoService;

    public ConsultaDocumentoDTO resolverDocumento(String numeroDocumento) {
        String documento = normalizarDocumento(numeroDocumento);
        String tipo = inferirTipoDocumento(documento);
        Cliente local = clienteRepository.findByNumeroDocumento(documento).orElse(null);
        if (local != null) {
            return new ConsultaDocumentoDTO(local.getIdCliente(), local.getTipoDocumento(), local.getNumeroDocumento(),
                    local.getNombreRazonSocial(), local.getDireccion(), true, false, "LOCAL",
                    "Cliente encontrado en la base local.");
        }

        ConsultaDocumentoService.Resultado externo = consultaDocumentoService.consultar(tipo, documento);
        if (externo.encontrado()) {
            return new ConsultaDocumentoDTO(null, tipo, documento, externo.nombreRazonSocial(), externo.direccion(),
                    true, true, "EXTERNO", externo.mensaje() == null
                            ? "Datos obtenidos de JSON.pe. Confirma antes de guardar."
                            : externo.mensaje());
        }
        return new ConsultaDocumentoDTO(null, tipo, documento, null, null, false, true, "MANUAL",
                externo.mensaje() == null ? "Completa el nombre para registrar al cliente." : externo.mensaje());
    }

    public ClienteDTO buscarPorDocumento(String termino) {
        Cliente c = clienteRepository.findFirstByNumeroDocumentoOrNombreRazonSocialContainingIgnoreCase(termino, termino)
                .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Cliente no encontrado con: " + termino));
        return mapToDTO(c);
    }

    public List<ClienteDTO> buscarPorCriterio(String criterio, String termino) {
        if ("NOMBRE".equalsIgnoreCase(criterio)) {
            return clienteRepository.findByNombreRazonSocialContainingIgnoreCase(termino).stream()
                    .map(this::mapToDTO).collect(Collectors.toList());
        } else {
            return clienteRepository.findByNumeroDocumento(termino).stream()
                    .map(this::mapToDTO).collect(Collectors.toList());
        }
    }

    public ClienteDTO registrarCliente(ClienteRequest request) {
        String tipo = normalizarTipo(request.tipoDocumento());
        String documento = normalizarDocumento(request.numeroDocumento());
        validarDocumento(tipo, documento);
        // Validar si ya existe
        if (clienteRepository.findByNumeroDocumento(documento).isPresent()) {
            throw new com.vircarmen.botica.exception.BusinessException("Ya existe un cliente con ese número de documento");
        }

        Cliente cliente = new Cliente();
        cliente.setTipoDocumento(tipo);
        cliente.setNumeroDocumento(documento);
        cliente.setNombreRazonSocial(request.nombreRazonSocial().trim());
        cliente.setDireccion(normalizar(request.direccion()));

        Cliente guardado = clienteRepository.save(cliente);
        return mapToDTO(guardado);
    }

    public List<ClienteDTO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ClienteDTO mapToDTO(Cliente c) {
        return new ClienteDTO(
                c.getIdCliente(),
                c.getTipoDocumento(),
                c.getNumeroDocumento(),
                c.getNombreRazonSocial()
        );
    }

    @Transactional
    public ClienteDTO actualizarCliente(Integer idCliente, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(Integer.valueOf(idCliente))
                .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Cliente no encontrado"));

        String tipo = normalizarTipo(request.tipoDocumento());
        String documento = normalizarDocumento(request.numeroDocumento());
        validarDocumento(tipo, documento);
        clienteRepository.findByNumeroDocumento(documento)
                .filter(otro -> !otro.getIdCliente().equals(idCliente))
                .ifPresent(otro -> { throw new BusinessException("Ya existe un cliente con ese número de documento"); });
        cliente.setTipoDocumento(tipo);
        cliente.setNumeroDocumento(documento);
        cliente.setNombreRazonSocial(request.nombreRazonSocial().trim());
        cliente.setDireccion(normalizar(request.direccion()));

        return mapToDTO(clienteRepository.save(cliente));
    }

    private String normalizarDocumento(String valor) {
        if (valor == null) throw new BusinessException("El documento es obligatorio.");
        return valor.replaceAll("\\s+", "").trim();
    }

    private String inferirTipoDocumento(String documento) {
        if (!documento.matches("\\d+")) {
            throw new BusinessException("El DNI o RUC debe contener solo números.");
        }
        if (documento.length() == 8) return "DNI";
        if (documento.length() == 11) return "RUC";
        throw new BusinessException("Ingresa un DNI de 8 dígitos o un RUC de 11 dígitos.");
    }

    private String normalizarTipo(String valor) {
        if (valor == null) throw new BusinessException("El tipo de documento es obligatorio.");
        return valor.trim().toUpperCase();
    }

    private void validarDocumento(String tipo, String documento) {
        if ("DNI".equals(tipo) && !documento.matches("\\d{8}")) {
            throw new BusinessException("El DNI debe tener 8 dígitos.");
        }
        if ("RUC".equals(tipo) && !documento.matches("\\d{11}")) {
            throw new BusinessException("El RUC debe tener 11 dígitos.");
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
