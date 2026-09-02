package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
    Optional<Cliente> findFirstByNumeroDocumentoOrNombreRazonSocialContainingIgnoreCase(String numeroDocumento, String nombreRazonSocial);
    java.util.List<Cliente> findByNombreRazonSocialContainingIgnoreCase(String nombreRazonSocial);
}