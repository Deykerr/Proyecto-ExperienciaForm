package com.vircarmen.botica.controller;

import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.Rol;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.dto.UsuarioDTO;
import com.vircarmen.botica.repository.RolRepository;
import com.vircarmen.botica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioService.listarUsuarios().stream().map(this::mapToDTO).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(mapToDTO(usuarioService.buscarPorId(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody Map<String, Object> datos) {
        String username = (String) datos.get("username");
        String password = (String) datos.get("password");
        Rol rol = obtenerRol(datos);
        Usuario creado = usuarioService.registrarEmpleado(
                username, password, (String) datos.get("nombreCompleto"), rol);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(mapToDTO(creado));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Integer id, @RequestBody Map<String, Object> datos) {
        String password = datos.get("password") instanceof String valor ? valor : null;
        Usuario actualizado = usuarioService.actualizarUsuario(
                id,
                (String) datos.get("nombreCompleto"),
                password,
                obtenerRol(datos));
        return ResponseEntity.ok(mapToDTO(actualizado));
    }

    @GetMapping("/roles")
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    // SOLO EL ADMIN PUEDE DAR DE BAJA A UN EMPLEADO
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstadoUsuario(
            @PathVariable Integer id, 
            @RequestParam EstadoGeneral estado) {
        
        usuarioService.cambiarEstadoUsuario(id, estado);
        return ResponseEntity.ok("Acceso del usuario modificado a: " + estado.name());
    }

    private Rol obtenerRol(Map<String, Object> datos) {
        if (datos.containsKey("rol")) {
            return Rol.valueOf(String.valueOf(datos.get("rol")).toUpperCase());
        }
        if (datos.get("rolId") instanceof Number rolId) {
            return rolRepository.findById(rolId.intValue())
                    .orElseThrow(() -> new com.vircarmen.botica.exception.BusinessException("Rol no válido"));
        }
        throw new com.vircarmen.botica.exception.BusinessException("El rol es obligatorio");
    }

    private UsuarioDTO mapToDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getNombreCompleto(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getEstado());
    }
}
