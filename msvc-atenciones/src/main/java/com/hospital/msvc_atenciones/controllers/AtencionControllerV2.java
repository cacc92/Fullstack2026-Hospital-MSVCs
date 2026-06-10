package com.hospital.msvc_atenciones.controllers;

import com.hospital.msvc_atenciones.assemblers.AtencionModelAssembler;
import com.hospital.msvc_atenciones.models.Atencion;
import com.hospital.msvc_atenciones.models.dtos.AtencionDTO;
import com.hospital.msvc_atenciones.services.AtencionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

/**
 * Controlador REST versión 2 para la gestión de atenciones.
 *
 * <p>Ofrece las operaciones CRUD y de consulta por médico/paciente devolviendo
 * representaciones HATEOAS ({@link EntityModel} / {@link CollectionModel}) con
 * enlaces de navegación. El listado completo conserva el DTO enriquecido con los
 * datos de médico y paciente.</p>
 */
@RestController
@RequestMapping("/api/v2/atenciones")
@Validated
@Tag(name = "Atenciones V2", description = "Metodos CRUD HATEOAS para la gestión de atenciones")
public class AtencionControllerV2 {

    @Autowired
    private AtencionService atencionService;

    @Autowired
    private AtencionModelAssembler atencionModelAssembler;

    /**
     * Lista todas las atenciones enriquecidas con datos de médico y paciente.
     *
     * @return colección HATEOAS de {@link AtencionDTO} con un enlace {@code self}
     */
    @GetMapping
    @Operation(
            summary = "Listado de todas las atenciones",
            description = "Se devuelve una colección HATEOAS con las atenciones enriquecidas"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<CollectionModel<AtencionDTO>> findAll() {
        List<AtencionDTO> atenciones = this.atencionService.findAll();
        CollectionModel<AtencionDTO> collectionModel = CollectionModel.of(
                atenciones,
                linkTo(methodOn(AtencionControllerV2.class).findAll()).withSelfRel()
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    /**
     * Busca una atención por su identificador.
     *
     * @param id identificador de la atención
     * @return la atención encontrada como recurso HATEOAS
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de una atencion por id",
            description = "Se devuelve una atencion, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atencion encontrada"),
            @ApiResponse(responseCode = "404", description = "Atencion no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<Atencion>> findById(
            @Parameter(description = "Id de la atencion a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        EntityModel<Atencion> entityModel = this.atencionModelAssembler.toModel(
                this.atencionService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
    }

    /**
     * Crea una nueva atención.
     *
     * @param atencion datos de la atención a crear (validados)
     * @return la atención creada como recurso HATEOAS
     */
    @PostMapping
    @Operation(summary = "Guardado de atencion", description = "Esta es la forma de guardar una atencion")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Atencion a crear", required = true,
            content = @Content(schema = @Schema(implementation = Atencion.class))
    )
    @ApiResponse(responseCode = "201", description = "Atencion creada")
    public ResponseEntity<EntityModel<Atencion>> save(@Valid @RequestBody Atencion atencion) {
        Atencion atencionCreate = this.atencionService.save(atencion);
        EntityModel<Atencion> entityModel = this.atencionModelAssembler.toModel(atencionCreate);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(entityModel);
    }

    /**
     * Actualiza una atención existente.
     *
     * @param id       identificador de la atención a actualizar
     * @param atencion nuevos datos de la atención (validados)
     * @return la atención actualizada como recurso HATEOAS
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de atencion", description = "Se actualizan los datos de una atencion existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atencion actualizada"),
            @ApiResponse(responseCode = "404", description = "Atencion no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<Atencion>> update(
            @Parameter(description = "Id de la atencion a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Atencion atencion
    ) {
        Atencion atencionUpdate = this.atencionService.updateById(atencion, id);
        EntityModel<Atencion> entityModel = this.atencionModelAssembler.toModel(atencionUpdate);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * Elimina una atención por su identificador.
     *
     * @param id identificador de la atención a eliminar
     * @return respuesta {@code 204 No Content}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminacion de atencion", description = "Se elimina una atencion por su id")
    @ApiResponse(responseCode = "204", description = "Atencion eliminada")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de la atencion a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        this.atencionService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Lista las atenciones asociadas a un médico.
     *
     * @param idMedico identificador del médico
     * @return colección HATEOAS de atenciones del médico
     */
    @GetMapping("/medico/{idMedico}")
    @Operation(
            summary = "Listado de atenciones por medico",
            description = "Se devuelven las atenciones asociadas a un medico como colección HATEOAS"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<Atencion>>> findByMedicoId(
            @Parameter(description = "Id del medico", required = true, example = "1")
            @PathVariable Long idMedico
    ) {
        List<EntityModel<Atencion>> entityModels = this.atencionService.findByMedicoId(idMedico)
                .stream()
                .map(atencionModelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<Atencion>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(AtencionControllerV2.class).findByMedicoId(idMedico)).withSelfRel()
        );
        return ResponseEntity.ok(collectionModel);
    }

    /**
     * Lista las atenciones asociadas a un paciente.
     *
     * @param idPaciente identificador del paciente
     * @return colección HATEOAS de atenciones del paciente
     */
    @GetMapping("/paciente/{idPaciente}")
    @Operation(
            summary = "Listado de atenciones por paciente",
            description = "Se devuelven las atenciones asociadas a un paciente como colección HATEOAS"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<Atencion>>> findByPacienteId(
            @Parameter(description = "Id del paciente", required = true, example = "1")
            @PathVariable Long idPaciente
    ) {
        List<EntityModel<Atencion>> entityModels = this.atencionService.findByPacienteId(idPaciente)
                .stream()
                .map(atencionModelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<Atencion>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(AtencionControllerV2.class).findByPacienteId(idPaciente)).withSelfRel()
        );
        return ResponseEntity.ok(collectionModel);
    }
}
