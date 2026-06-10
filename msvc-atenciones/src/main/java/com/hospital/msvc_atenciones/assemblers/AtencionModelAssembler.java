package com.hospital.msvc_atenciones.assemblers;

import com.hospital.msvc_atenciones.controllers.AtencionControllerV2;
import com.hospital.msvc_atenciones.models.Atencion;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Ensamblador HATEOAS para la entidad {@link Atencion}.
 *
 * <p>Transforma una {@code Atencion} en un {@link EntityModel} agregando los
 * enlaces de navegación (self y colección) expuestos por la API v2.</p>
 */
@Component
public class AtencionModelAssembler implements RepresentationModelAssembler<Atencion, EntityModel<Atencion>> {

    /**
     * Construye la representación HATEOAS de una atención.
     *
     * @param atencion entidad de dominio a representar
     * @return modelo con la atención y los enlaces {@code self} y {@code atenciones}
     */
    @Override
    public EntityModel<Atencion> toModel(Atencion atencion) {
        return EntityModel.of(
                atencion,
                linkTo(methodOn(AtencionControllerV2.class).findById(atencion.getAtencionId())).withSelfRel(),
                linkTo(methodOn(AtencionControllerV2.class).findAll()).withRel("atenciones"));
    }
}
