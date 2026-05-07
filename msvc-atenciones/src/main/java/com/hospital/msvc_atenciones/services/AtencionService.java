package com.hospital.msvc_atenciones.services;

import com.hospital.msvc_atenciones.models.Atencion;

import java.util.List;

public interface AtencionService {
    List<Atencion> findAll();
    Atencion findByID(Long id);
    Atencion save(Atencion atencion);
    Atencion updateById(Atencion atencion, Long id);
    void deleteById(Long id);
    List<Atencion> findByMedicoId(Long medicoId);
    List<Atencion> findByPacienteId(Long pacienteId);
}
