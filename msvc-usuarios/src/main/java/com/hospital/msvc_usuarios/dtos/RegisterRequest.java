package com.hospital.msvc_usuarios.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

// Datos para crear un usuario. 'roles' es opcional: si viene vacio se asigna ROLE_PACIENTE.
// Ejemplo: {"username":"dr.house","password":"1234","roles":["ROLE_MEDICO"]}
@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private Set<String> roles = new HashSet<>();
}
