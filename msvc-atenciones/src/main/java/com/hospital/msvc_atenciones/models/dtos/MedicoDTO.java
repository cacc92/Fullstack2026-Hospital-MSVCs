package com.hospital.msvc_atenciones.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
@NoArgsConstructor
public class MedicoDTO {
    private Long medicoId;
    private String run;
    private String nombreCompleto;
    private Boolean jefeTurno;
}
