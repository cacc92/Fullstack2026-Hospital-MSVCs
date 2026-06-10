package com.hospital.msvc_pacientes.assemblers;

import com.hospital.msvc_pacientes.controllers.PacienteControllerV2;
import com.hospital.msvc_pacientes.models.Paciente;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Ensamblador HATEOAS para la entidad {@link Paciente}.
 *
 * <p>Convierte un {@code Paciente} en un {@link EntityModel} agregando los
 * enlaces de navegación de la API (self y colección) que consume el cliente.</p>
 */
@Component
public class PacienteModelAssembler implements RepresentationModelAssembler<Paciente, EntityModel<Paciente>> {

    /**
     * Construye la representación HATEOAS de un paciente.
     *
     * @param paciente entidad de dominio a representar
     * @return modelo con el paciente y los enlaces {@code self} y {@code pacientes}
     */
    @Override
    public EntityModel<Paciente> toModel(Paciente paciente) {
        return EntityModel.of(
                paciente,
                linkTo(methodOn(PacienteControllerV2.class).findById(paciente.getPacienteId())).withSelfRel(),
                linkTo(methodOn(PacienteControllerV2.class).findAll()).withRel("pacientes"));
    }
}
