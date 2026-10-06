package com.vircarmen.botica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vircarmen.botica.dto.ConsultaDocumentoDTO;
import com.vircarmen.botica.entity.Cliente;
import com.vircarmen.botica.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ConsultaDocumentoService consultaDocumentoService;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void deberiaResolverPrimeroDesdeLaBaseLocal() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(10);
        cliente.setTipoDocumento("DNI");
        cliente.setNumeroDocumento("12345678");
        cliente.setNombreRazonSocial("ANA TORRES");
        when(clienteRepository.findByNumeroDocumento("12345678")).thenReturn(Optional.of(cliente));

        ConsultaDocumentoDTO resultado = clienteService.resolverDocumento("12345678");

        assertEquals(10, resultado.idCliente());
        assertEquals("LOCAL", resultado.origen());
        assertFalse(resultado.requiereRegistro());
        verifyNoInteractions(consultaDocumentoService);
    }

    @Test
    void deberiaProponerRegistroCuandoElProveedorDevuelveDatos() {
        when(clienteRepository.findByNumeroDocumento("12345678")).thenReturn(Optional.empty());
        when(consultaDocumentoService.consultar("DNI", "12345678"))
                .thenReturn(new ConsultaDocumentoService.Resultado(true, "ANA TORRES", "AYACUCHO", null));

        ConsultaDocumentoDTO resultado = clienteService.resolverDocumento("12345678");

        assertTrue(resultado.encontrado());
        assertTrue(resultado.requiereRegistro());
        assertEquals("EXTERNO", resultado.origen());
        assertEquals("ANA TORRES", resultado.nombreRazonSocial());
    }

    @Test
    void deberiaPermitirRegistroManualSiLaConsultaNoEstaConfigurada() {
        when(clienteRepository.findByNumeroDocumento("12345678")).thenReturn(Optional.empty());
        when(consultaDocumentoService.consultar("DNI", "12345678"))
                .thenReturn(new ConsultaDocumentoService.Resultado(false, null, null,
                        "La consulta automática no está configurada."));

        ConsultaDocumentoDTO resultado = clienteService.resolverDocumento("12345678");

        assertFalse(resultado.encontrado());
        assertTrue(resultado.requiereRegistro());
        assertEquals("MANUAL", resultado.origen());
    }
}
