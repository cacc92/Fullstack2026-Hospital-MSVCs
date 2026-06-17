package com.hospital.msvc_usuarios.repositories;

import com.hospital.msvc_usuarios.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositorio de usuarios. Al extender JpaRepository ya hereda save, findById, findAll, etc.
// Los metodos de abajo son "consultas derivadas": Spring Data genera el SQL leyendo el nombre del metodo.
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // findBy + Username  ->  SELECT * FROM usuarios WHERE username = ?  (lo usa el login)
    Optional<Usuario> findByUsername(String username);
    // existsBy + Username  ->  devuelve true/false (lo usa el registro para evitar duplicados)
    boolean existsByUsername(String username);
}
