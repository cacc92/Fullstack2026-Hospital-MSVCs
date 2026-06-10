package com.hospital.msvc_medicos.assemblers;

import com.hospital.msvc_medicos.controllers.MedicoControllerV2;
import com.hospital.msvc_medicos.models.Medico;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

// @Component: registra esta clase como bean para poder inyectarla en el controlador V2.
// RepresentationModelAssembler<Medico, EntityModel<Medico>>: convierte un Medico (entidad)
// en un EntityModel (la entidad + enlaces HATEOAS), todo en un solo lugar reutilizable.
@Component
public class MedicoModelAssembler implements RepresentationModelAssembler<Medico, EntityModel<Medico>> {

    @Override
    public EntityModel<Medico> toModel(Medico medico) {
        // EntityModel.of(datos, ...enlaces) empaqueta el medico junto a sus links.
        return EntityModel.of(
                medico,
                // self: enlace al propio recurso (GET /api/v2/medicos/{id}). linkTo + methodOn
                // arman la URL leyendo el mapeo del metodo, sin escribirla a mano.
                linkTo(methodOn(MedicoControllerV2.class).findById(medico.getMedicoId())).withSelfRel(),
                // medicos: enlace al listado completo, para que el cliente sepa como navegar.
                linkTo(methodOn(MedicoControllerV2.class).findAll()).withRel("medicos"));
    }
}
