package com.hospital.msvc_atenciones.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PacienteDTO {
    private Long pacienteId;
    private String rut;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String correo;
}
