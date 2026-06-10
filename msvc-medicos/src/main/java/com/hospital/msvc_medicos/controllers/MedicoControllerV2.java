package com.hospital.msvc_medicos.controllers;

import com.hospital.msvc_medicos.assemblers.MedicoModelAssembler;
import com.hospital.msvc_medicos.models.Medico;
import com.hospital.msvc_medicos.models.dtos.MedicoDTO;
import com.hospital.msvc_medicos.services.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

// Misma idea que V1 pero ruta /api/v2 y respuestas HATEOAS: ademas de los datos
// se devuelven enlaces para navegar la API (EntityModel = 1 recurso, CollectionModel = lista).
@RestController
@RequestMapping("/api/v2/medicos")
@Validated
@Tag(name="Medicos V2", description = "Metodos CRUD para la gestión de medicos")
public class MedicoControllerV2 {

    @Autowired
    private MedicoService medicoService;

    // El assembler arma los enlaces HATEOAS de cada medico (ver MedicoModelAssembler).
    @Autowired
    private MedicoModelAssembler medicoModelAssembler;

    @GetMapping
    @Operation(
            summary = "Listado de todos los medicos",
            description = "Se devuelve una lista con los medicos que se encuentran en la tabla medicos de la DB"

    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<Medico>>> findAll() {
        // 1) Traer medicos y convertir cada uno en EntityModel (medico + sus enlaces).
        List<EntityModel<Medico>> entityModels = this.medicoService.findAll()
                .stream()
                .map(medicoModelAssembler::toModel)
                .toList();
        // 2) Envolver la lista en un CollectionModel y agregarle su propio enlace self.
        CollectionModel<EntityModel<Medico>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(MedicoControllerV2.class).findAll()).withSelfRel()
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un medico",
            description = "Se devuelve un medico, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value={
            @ApiResponse(
                    responseCode = "200",
                    description = "Medico encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicoDTO.class),
                            examples = {
                                @ExampleObject(
                                        name = "Ejemplo Medico",
                                        value = "{\"rut\": \"1-1\", \"nombreComplto\": \"Dr. House\", \"jefeTurno\": true}"
                                )
                            }
                    )),
            @ApiResponse(responseCode = "404", description = "Medico no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<Medico>> findById(
            @Parameter(description = "Id del medico a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        // Buscar el medico y pedirle al assembler que le agregue los enlaces.
        EntityModel<Medico> entityModel = this.medicoModelAssembler.toModel(
                this.medicoService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
    }

    @PostMapping
    @Operation(summary = "Guardado de medico", description = "Esta es la forma de guardar un medico")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Medico a crear", required = true,
            content = @Content(schema = @Schema(implementation = MedicoDTO.class))
    )
    public ResponseEntity<EntityModel<Medico>> save(@Valid @RequestBody Medico medico) {
        Medico medicoCreate = this.medicoService.save(medico);
        EntityModel<Medico> entityModel = this.medicoModelAssembler.toModel(medicoCreate);
        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de medico", description = "Se actualizan los datos de un medico existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medico actualizado"),
            @ApiResponse(responseCode = "404", description = "Medico no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<Medico>> update(
            @Parameter(description = "Id del medico a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Medico medico
    ) {
        Medico medicoUpdate = this.medicoService.updateById(id, medico);
        EntityModel<Medico> entityModel = this.medicoModelAssembler.toModel(medicoUpdate);
        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminacion de medico", description = "Se elimina un medico y sus atenciones asociadas")
    @ApiResponse(responseCode = "204", description = "Medico eliminado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del medico a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        this.medicoService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
