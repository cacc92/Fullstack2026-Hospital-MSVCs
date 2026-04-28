package com.hospital.msvc_atenciones.clients;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "msvc-medicos", url = "https://localhost:8001/api/v1/medicos")
public interface MedicoClient {
}
