package com.hospital.msvc_atenciones.repositories;

import com.hospital.msvc_atenciones.models.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    List<Atencion> findByMedicoId(Long medicoId);

}
