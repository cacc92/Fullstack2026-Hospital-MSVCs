package com.hospital.msvc_pacientes.models.dtos;

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
    private Long atencionId;
    private LocalDateTime horaAtencion;
    private Double costo;
    private String comentario;
    private Long medicoId;
    private Long pacienteId;
}
