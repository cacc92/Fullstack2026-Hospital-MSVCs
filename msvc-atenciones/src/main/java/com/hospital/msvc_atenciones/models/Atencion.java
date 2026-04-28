package com.hospital.msvc_atenciones.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "atenciones")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Atencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="atencion_id")
    private Long atencionId;

    @NotNull(message = "El campo hora atencion no puede ser nulo")
    @Column(name = "hora_atencion")
    private LocalDateTime horaAtencion;

    @NotNull(message = "El campo de costo no puede ser vacio")
    @Column(nullable = false)
    private Double costo;

    private String comentario;

    @Embedded
    Audit audit =  new Audit();

    @NotNull(message = "El campo medico no puede ser vacio")
    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    @NotNull(message = "El campo paciente no puede ser vacio")
    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;


}
