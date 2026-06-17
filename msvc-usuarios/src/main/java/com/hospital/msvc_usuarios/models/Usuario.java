package com.hospital.msvc_usuarios.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

// Un usuario del sistema. Puede representar a un medico o a un paciente segun sus roles.
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long usuarioId;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    // Se guarda CIFRADA con BCrypt, nunca en texto plano.
    @NotBlank
    @Column(nullable = false)
    private String password;

    // Relacion muchos-a-muchos: un usuario tiene varios roles y un rol lo comparten varios usuarios.
    // Se crea una tabla intermedia 'usuario_roles' con las dos llaves foraneas.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();
}
