package com.hospital.msvc_atenciones.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class AtencionDTO {

    private Long id;
    private LocalDateTime horaAtencion;
    private Double costo;
    private String comentario;
    private PersonaDTO paciente;
    private PersonaDTO medico;
}
