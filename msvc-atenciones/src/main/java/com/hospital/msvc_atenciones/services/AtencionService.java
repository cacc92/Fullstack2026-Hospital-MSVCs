package com.hospital.msvc_atenciones.services;

import com.hospital.msvc_atenciones.models.Atencion;
import com.hospital.msvc_atenciones.models.dtos.AtencionDTO;

import java.util.List;

public interface AtencionService {

    List<AtencionDTO> findAll();
    Atencion findById(Long id);
    Atencion save(Atencion atencion);
    Atencion updateById(Atencion atencion, Long id);
    void deleteById(Long id);
    List<Atencion> findByPacienteId(Long idPaciente);
    List<Atencion> findByMedicoId(Long idMedico);
}
