package com.hospital.msvc_atenciones.clients;

import com.hospital.msvc_atenciones.models.dtos.MedicoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-medicos", url = "https://localhost:8001/api/v1/medicos")
public interface MedicoClient {

    @GetMapping("/{id}")
    MedicoDTO findById(@PathVariable Long id);

}
