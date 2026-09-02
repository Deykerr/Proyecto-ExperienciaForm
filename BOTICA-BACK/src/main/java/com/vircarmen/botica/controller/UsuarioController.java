package com.vircarmen.botica.controller;

import com.vircarmen.botica.entity.EstadoGeneral;
import com.vircarmen.botica.entity.Rol;
import com.vircarmen.botica.entity.Usuario;
import com.vircarmen.botica.repository.RolRepository;
import com.vircarmen.botica.repository.UsuarioRepository;
import com.vircarmen.botica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Map<String, Object> datos) {
        String username = (String) datos.get("username");
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Usuario ya existe");
        }
        Usuario u = new Usuario();
        u.setNombreCompleto((String) datos.get("nombreCompleto"));
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode((String) datos.get("password")));
        
        if (datos.containsKey("rol")) {
            u.setRol(Rol.valueOf(String.valueOf(datos.get("rol")).toUpperCase()));
        } else if (datos.containsKey("rolId")) {
            Integer rolId = (Integer) datos.get("rolId");
            Rol rol = rolRepository.findById(rolId).orElseThrow();
            u.setRol(rol);
        }
        
        return ResponseEntity.ok(usuarioRepository.save(u));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id, @RequestBody Map<String, Object> datos) {
        return usuarioRepository.findById(id).map(u -> {
            u.setNombreCompleto((String) datos.get("nombreCompleto"));
            if (datos.containsKey("password") && datos.get("password") != null && !((String)datos.get("password")).isEmpty()) {
                u.setPasswordHash(passwordEncoder.encode((String) datos.get("password")));
            }
            if (datos.containsKey("rol")) {
                u.setRol(Rol.valueOf(String.valueOf(datos.get("rol")).toUpperCase()));
            } else if (datos.containsKey("rolId")) {
                Integer rolId = (Integer) datos.get("rolId");
                Rol rol = rolRepository.findById(rolId).orElseThrow();
                u.setRol(rol);
            }
            return ResponseEntity.ok(usuarioRepository.save(u));
        }).orElse(ResponseEntity.notFound().build());
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
}
