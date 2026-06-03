package com.hospital.msvc_medicos.assemblers;

import com.hospital.msvc_medicos.controllers.MedicoControllerV2;
import com.hospital.msvc_medicos.models.Medico;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class MedicoModelAssembler implements RepresentationModelAssembler<Medico, EntityModel<Medico>> {

    @Override
    public EntityModel<Medico> toModel(Medico medico) {
        return EntityModel.of(
                medico,
                linkTo(methodOn(MedicoControllerV2.class).findById(medico.getMedicoId())).withSelfRel(),
                linkTo(methodOn(MedicoControllerV2.class).findAll()).withRel("medicos"));
    }
}
