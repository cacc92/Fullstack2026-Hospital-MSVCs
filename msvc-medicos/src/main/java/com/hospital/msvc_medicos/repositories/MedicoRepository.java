package com.hospital.msvc_medicos.repositories;

import com.hospital.msvc_medicos.models.Medico;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicoRepository {
    Optional<Medico> findByRun(String run);
}
