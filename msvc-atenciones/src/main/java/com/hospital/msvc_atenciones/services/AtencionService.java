package com.hospital.msvc_atenciones.services;

import com.hospital.msvc_atenciones.models.Atencion;

import java.util.List;

public interface AtencionService {
    List<Atencion> findAll();
    Atencion save(Atencion atencion);
}
