package com.hospital.msvc_usuarios.config;

import com.hospital.msvc_usuarios.models.Rol;
import com.hospital.msvc_usuarios.models.Usuario;
import com.hospital.msvc_usuarios.repositories.RolRepository;
import com.hospital.msvc_usuarios.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

// Siembra datos al arrancar: los 3 roles y 3 usuarios de prueba (uno por rol).
// Asi la demo tiene credenciales listas sin tener que registrarse a mano.
@Component
public class DataLoader implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rol admin = obtenerOCrearRol("ROLE_ADMIN");
        Rol medico = obtenerOCrearRol("ROLE_MEDICO");
        Rol paciente = obtenerOCrearRol("ROLE_PACIENTE");

        crearUsuarioSiNoExiste("admin", "admin123", Set.of(admin));
        crearUsuarioSiNoExiste("medico1", "medico123", Set.of(medico));
        crearUsuarioSiNoExiste("paciente1", "paciente123", Set.of(paciente));
    }

    private Rol obtenerOCrearRol(String nombre) {
        return this.rolRepository.findByNombre(nombre).orElseGet(() -> this.rolRepository.save(new Rol(nombre)));
    }

    private void crearUsuarioSiNoExiste(String username, String passwordPlano, Set<Rol> roles) {
        if (this.usuarioRepository.existsByUsername(username)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(this.passwordEncoder.encode(passwordPlano));
        usuario.setRoles(roles);
        this.usuarioRepository.save(usuario);
    }
}
