package com.hospital.msvc_atenciones.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PersonaDTO {
    private Long id;
    private String rut;
    private String nombreCompleto;
}
