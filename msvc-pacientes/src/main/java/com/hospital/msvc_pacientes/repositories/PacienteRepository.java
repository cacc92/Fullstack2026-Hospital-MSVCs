package com.hospital.msvc_pacientes.repositories;

import com.hospital.msvc_pacientes.models.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    // Método que me permite buscar por rut
    Optional<Paciente> findByRut(String rut);

    // Método que me permite buscar por correo
    Optional<Paciente> findByCorreo(String correo);
}
