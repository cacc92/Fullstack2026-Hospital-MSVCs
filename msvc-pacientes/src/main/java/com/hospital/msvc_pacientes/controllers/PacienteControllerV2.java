package com.hospital.msvc_pacientes.controllers;

import com.hospital.msvc_pacientes.assemblers.PacienteModelAssembler;
import com.hospital.msvc_pacientes.models.Paciente;
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

import com.hospital.msvc_pacientes.services.PacienteService;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Controlador REST versión 2 para la gestión de pacientes.
 *
 * <p>Expone las mismas operaciones CRUD que la versión 1 pero devuelve
 * representaciones HATEOAS ({@link EntityModel} / {@link CollectionModel}),
 * incluyendo enlaces de navegación entre recursos.</p>
 */
@RestController
@RequestMapping("/api/v2/pacientes")
@Validated
@Tag(name = "Pacientes V2", description = "Metodos CRUD HATEOAS para la gestión de pacientes")
public class PacienteControllerV2 {

    @Autowired
    private PacienteService pacienteService;

    // El assembler arma los enlaces HATEOAS de cada paciente (ver PacienteModelAssembler).
    @Autowired
    private PacienteModelAssembler pacienteModelAssembler;

    /**
     * Lista todos los pacientes como colección HATEOAS.
     *
     * @return colección de pacientes con sus enlaces y un enlace {@code self}
     */
    @GetMapping
    @Operation(
            summary = "Listado de todos los pacientes",
            description = "Se devuelve una colección HATEOAS con los pacientes de la DB"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<Paciente>>> findAll() {
        // 1) Convertir cada paciente en EntityModel (paciente + sus enlaces).
        List<EntityModel<Paciente>> entityModels = this.pacienteService.findAll()
                .stream()
                .map(pacienteModelAssembler::toModel)
                .toList();
        // 2) Envolver la lista en un CollectionModel con su propio enlace self.
        CollectionModel<EntityModel<Paciente>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(PacienteControllerV2.class).findAll()).withSelfRel()
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    /**
     * Busca un paciente por su identificador.
     *
     * @param id identificador del paciente
     * @return el paciente encontrado como recurso HATEOAS
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un paciente por id",
            description = "Se devuelve un paciente, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<Paciente>> findById(
            @Parameter(description = "Id del paciente a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        EntityModel<Paciente> entityModel = this.pacienteModelAssembler.toModel(
                this.pacienteService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
    }

    /**
     * Crea un nuevo paciente.
     *
     * @param paciente datos del paciente a crear (validados)
     * @return el paciente creado como recurso HATEOAS
     */
    @PostMapping
    @Operation(summary = "Guardado de paciente", description = "Esta es la forma de guardar un paciente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Paciente a crear", required = true,
            content = @Content(schema = @Schema(implementation = Paciente.class))
    )
    @ApiResponse(responseCode = "201", description = "Paciente creado")
    public ResponseEntity<EntityModel<Paciente>> save(@Valid @RequestBody Paciente paciente) {
        Paciente pacienteCreate = this.pacienteService.save(paciente);
        EntityModel<Paciente> entityModel = this.pacienteModelAssembler.toModel(pacienteCreate);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(entityModel);
    }

    /**
     * Actualiza un paciente existente.
     *
     * @param id       identificador del paciente a actualizar
     * @param paciente nuevos datos del paciente (validados)
     * @return el paciente actualizado como recurso HATEOAS
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de paciente", description = "Se actualizan los datos de un paciente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente actualizado"),
            @ApiResponse(responseCode = "404", description = "Paciente no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<Paciente>> update(
            @Parameter(description = "Id del paciente a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Paciente paciente
    ) {
        Paciente pacienteUpdate = this.pacienteService.updateById(id, paciente);
        EntityModel<Paciente> entityModel = this.pacienteModelAssembler.toModel(pacienteUpdate);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * Elimina un paciente y sus atenciones asociadas.
     *
     * @param id identificador del paciente a eliminar
     * @return respuesta {@code 204 No Content}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminacion de paciente", description = "Se elimina un paciente y sus atenciones asociadas")
    @ApiResponse(responseCode = "204", description = "Paciente eliminado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del paciente a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        this.pacienteService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
