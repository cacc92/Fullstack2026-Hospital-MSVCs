package com.hospital.msvc_pacientes.services;

import com.hospital.msvc_pacientes.models.Paciente;

import java.util.List;

public interface PacienteService {
    List<Paciente> findAll();
    Paciente findById(Long id);
    Paciente findByCorreo(String correo);
    Paciente findByRut(String rut);
    Paciente save(Paciente paciente);
    void deleteById(Long id);
    Paciente updateById(Long id, Paciente paciente);
}
