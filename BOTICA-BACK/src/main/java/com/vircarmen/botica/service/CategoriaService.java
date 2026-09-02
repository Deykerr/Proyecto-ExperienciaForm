package com.vircarmen.botica.service;

import com.vircarmen.botica.entity.Categoria;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public Categoria registrarCategoria(String nombre, String descripcion) {
        if (categoriaRepository.existsByNombre(nombre.trim())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre.trim());
        categoria.setDescripcion(descripcion);
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Integer idCategoria) {
        return categoriaRepository.findById(Integer.valueOf(idCategoria))
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
    }

    @Transactional
    public Categoria actualizarCategoria(Integer idCategoria, String nombre, String descripcion) {
        Categoria categoria = buscarPorId(idCategoria);
        categoria.setNombre(nombre.trim());
        categoria.setDescripcion(descripcion);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria cambiarEstadoCategoria(Integer idCategoria, EstadoGeneral estado) {
        Categoria categoria = buscarPorId(idCategoria);
        categoria.setEstado(estado);
        return categoriaRepository.save(categoria);
    }
}