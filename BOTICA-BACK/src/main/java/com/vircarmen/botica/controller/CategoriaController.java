package com.vircarmen.botica.controller;

import com.vircarmen.botica.entity.Categoria;
import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerCategoria(@PathVariable Integer id) {
        return categoriaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> guardarCategoria(@RequestBody Categoria categoria) {
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            return ResponseEntity.badRequest().body("Categoría con ese nombre ya existe");
        }
        return ResponseEntity.ok(categoriaRepository.save(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Integer id, @RequestBody Categoria datos) {
        return categoriaRepository.findById(id).map(cat -> {
            cat.setNombre(datos.getNombre());
            cat.setDescripcion(datos.getDescripcion());
            return ResponseEntity.ok(categoriaRepository.save(cat));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Categoria> cambiarEstado(@PathVariable Integer id, @RequestParam String estado) {
        return categoriaRepository.findById(id).map(cat -> {
            cat.setEstado(EstadoGeneral.valueOf(estado));
            return ResponseEntity.ok(categoriaRepository.save(cat));
        }).orElse(ResponseEntity.notFound().build());
    }
}
