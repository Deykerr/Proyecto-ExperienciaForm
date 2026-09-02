    package com.vircarmen.botica.entity;

    import java.time.LocalDateTime;
    import java.util.Collection;
    import java.util.List;

    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;

    import com.fasterxml.jackson.annotation.JsonProperty;

    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.EnumType;
    import jakarta.persistence.Enumerated;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.Table;

    @Entity
    @Table(name = "usuarios")
    public class Usuario implements UserDetails {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer idUsuario;

        @Column(name = "nombre_completo", length = 200, nullable = false)
        private String nombreCompleto;

        @Column(length = 50, nullable = false, unique = true)
        private String username;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Column(name = "password_hash", length = 255, nullable = false)
        private String passwordHash;

        // AQUÍ USA TU ENUM ORIGINAL INTACTO
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private Rol rol;

        @Enumerated(EnumType.STRING)
        @Column(length = 1)
        private EstadoGeneral estado = EstadoGeneral.A;

        @Column(name = "fecha_creacion", updatable = false)
        private LocalDateTime fechaCreacion = LocalDateTime.now();

        public Integer getIdUsuario() { return idUsuario; }
        public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
        public String getNombreCompleto() { return nombreCompleto; }
        public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
        public Rol getRol() { return rol; }
        public void setRol(Rol rol) { this.rol = rol; }
        public EstadoGeneral getEstado() { return estado; }
        public void setEstado(EstadoGeneral estado) { this.estado = estado; }
        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }


        // === LA MAGIA DE SPRING SECURITY ===
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Extraemos tu Enum "ADMIN" y le sumamos "ROLE_" para formar "ROLE_ADMIN" en memoria
            return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol.name()));
        }

        @Override
        public String getPassword() {
            return this.passwordHash;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return this.estado == EstadoGeneral.A;
        }
    }