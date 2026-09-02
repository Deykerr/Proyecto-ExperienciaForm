package com.vircarmen.botica.service;

import com.vircarmen.botica.entity.Categoria;
import com.vircarmen.botica.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void registrarCategoriaDeberiaRechazarNombreDuplicado() {
        when(categoriaRepository.existsByNombre("Analgesicos")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> categoriaService.registrarCategoria("Analgesicos", "Desc"));
    }
}
