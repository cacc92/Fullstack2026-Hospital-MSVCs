package com.hospital.msvc_pacientes.clients;

import com.hospital.msvc_pacientes.models.dtos.AtencionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="msvc-atencion", url = "localhost:8002/api/v1/atenciones")
public interface AtencionClient {

    @GetMapping("/paciente/{idPaciente}")
    List<AtencionDTO> getAtencionByIdPaciente(@PathVariable Long idPaciente);

    @DeleteMapping("/{id}")
    void deleteAtencionById(@PathVariable Long id);
}
