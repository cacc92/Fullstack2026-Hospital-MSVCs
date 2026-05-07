package com.hospital.msvc_pacientes.clients;

import com.hospital.msvc_pacientes.models.dtos.AtencionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name="msvc-atenciones", url="localhost:8000/api/v1/atenciones")
public interface AtencionClient {

    @GetMapping("/paciente/{pacienteId}")
    List<AtencionDTO> getAtencionesByPacienteId(@PathVariable Long pacienteId);

    @DeleteMapping("/{id}")
    void deleteAtencionById(@PathVariable Long id);
}
