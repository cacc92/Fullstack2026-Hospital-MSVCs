package com.hospital.msvc_medicos.clients;

import com.hospital.msvc_medicos.models.dtos.AtencionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name="msvc-atenciones", url = "https://localhost:8002/api/v1/atenciones")
public interface AtencionClient {

    @GetMapping("/medico/{idMedico}")
    List<AtencionDTO> getAtencionesByIdMedico(@PathVariable Long idMedico);

    @DeleteMapping("/{id}")
    void deleteAtencionById(@PathVariable Long id);
}
