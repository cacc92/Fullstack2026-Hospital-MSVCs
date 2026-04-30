package com.hospital.msvc_atenciones.repositories;

import com.hospital.msvc_atenciones.models.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtencionRepository extends JpaRepository<Atencion, Long> {
}
