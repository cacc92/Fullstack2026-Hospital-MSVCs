package com.hospital.msvc_atenciones.clients;

import com.hospital.msvc_atenciones.models.dtos.PacienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="msvc-pacientes", url = "https://localhost:8000/api/v1/pacientes")
public interface PacienteClient {
    @GetMapping("/{id}")
    PacienteDTO getPacienteById(@PathVariable Long id);
}
