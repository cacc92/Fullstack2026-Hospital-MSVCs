package com.hospital.msvc_medicos.services;

import com.hospital.msvc_medicos.models.Medico;
import com.hospital.msvc_medicos.models.dtos.AtencionDTO;

import java.util.List;

public interface MedicoService {
    List<Medico> findAll();
    Medico findById(Long id);
    Medico findByRun(String run);
    Medico save(Medico medico);
    void deleteById(Long id);
    Medico updateById(Long id, Medico medico);
    // List<AtencionDTO> findAtencionByMedico(Long id);
}
