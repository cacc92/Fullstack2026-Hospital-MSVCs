package com.hospital.msvc_medicos.models.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MedicoDTO {

    @NotBlank(message = "El campo run no puede ser vacio")
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "El formato del run debe ser xxxxxxxx-x")
    private String run;

    @NotBlank(message = "El campo nombre completo no puede ser vacio")
    private String nombreCompleto;

    @Column(name="jefe_turno", nullable = false)
    private Boolean jefeTurno;

}
