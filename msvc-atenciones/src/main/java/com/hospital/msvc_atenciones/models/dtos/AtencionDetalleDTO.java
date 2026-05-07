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
public class AtencionDetalleDTO {
    private Long atencionId;
    private LocalDateTime horaAtencion;
    private Double costo;
    private String comentario;
    private PersonaDTO medico;
    private PersonaDTO paciente;
}
