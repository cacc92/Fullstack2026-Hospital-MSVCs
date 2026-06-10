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
// @Component: registra esta clase como bean para poder inyectarla en el controlador V2.
// RepresentationModelAssembler<Paciente, EntityModel<Paciente>>: convierte un Paciente (entidad)
// en un EntityModel (la entidad + enlaces HATEOAS), todo en un solo lugar reutilizable.
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
        // EntityModel.of(datos, ...enlaces) empaqueta el paciente junto a sus links.
        return EntityModel.of(
                paciente,
                // self: enlace al propio recurso (GET /api/v2/pacientes/{id}). linkTo + methodOn
                // arman la URL leyendo el mapeo del metodo, sin escribirla a mano.
                linkTo(methodOn(PacienteControllerV2.class).findById(paciente.getPacienteId())).withSelfRel(),
                // pacientes: enlace al listado completo, para que el cliente sepa como navegar.
                linkTo(methodOn(PacienteControllerV2.class).findAll()).withRel("pacientes"));
    }
}
