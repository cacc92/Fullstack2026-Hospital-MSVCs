package com.hospital.msvc_usuarios.repositories;

import com.hospital.msvc_usuarios.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositorio de roles. Hereda los metodos CRUD de JpaRepository.
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    // Busca un rol por su nombre (ej: "ROLE_MEDICO"). Lo usan el registro y el seed inicial.
    Optional<Rol> findByNombre(String nombre);
}
