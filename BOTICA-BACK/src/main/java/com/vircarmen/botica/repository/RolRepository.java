package com.vircarmen.botica.repository;

import com.vircarmen.botica.entity.Rol;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class RolRepository {

    public List<Rol> findAll() {
        return Arrays.asList(Rol.values());
    }

    public Optional<Rol> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        Rol[] roles = Rol.values();
        if (id >= 0 && id < roles.length) {
            return Optional.of(roles[id]);
        }
        return Optional.empty();
    }

    public Rol save(Rol rol) {
        return rol;
    }

    public long count() {
        return Rol.values().length;
    }
}
