package com.hospital.msvc_medicos.controllers;

import com.hospital.msvc_medicos.models.Medico;
import com.hospital.msvc_medicos.models.dtos.MedicoDTO;
import com.hospital.msvc_medicos.services.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController: cada metodo devuelve datos (JSON), no vistas HTML.
// @RequestMapping: prefijo comun de las rutas. Aqui es la version 1 de la API.
// @Validated: activa la validacion de los @Valid de los metodos.
// @Tag: agrupa estos endpoints bajo un nombre en Swagger UI.
@RestController
@RequestMapping("/api/v1/medicos")
@Validated
@Tag(name="Medicos V1", description = "Metodos CRUD para la gestión de medicos")
public class MedicoController {

    // @Autowired: Spring inyecta automaticamente la implementacion del servicio.
    @Autowired
    private MedicoService medicoService;

    // @GetMapping sin ruta => GET /api/v1/medicos
    // @Operation y @ApiResponse son solo documentacion: describen el endpoint en Swagger.
    @GetMapping
    @Operation(
            summary = "Listado de todos los medicos",
            description = "Se devuelve una lista con los medicos que se encuentran en la tabla medicos de la DB"

    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<Medico>> findAll() {
        // ResponseEntity.ok(...) = cuerpo + codigo HTTP 200.
        return ResponseEntity.ok(this.medicoService.findAll());
    }

    // {id} es una variable de ruta: GET /api/v1/medicos/5 => id = 5.
    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un medico",
            description = "Se devuelve un medico, en caso contrario se devuelve una excepcion"
    )
    // @ApiResponses documenta los posibles codigos de respuesta. @Content/@Schema/@ExampleObject
    // muestran en Swagger la forma del JSON y un ejemplo concreto.
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
    public ResponseEntity<Medico> findById(
            // @PathVariable toma el valor de {id} de la URL. @Parameter solo lo documenta.
            @Parameter(description = "Id del medico a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(this.medicoService.findById(id));
    }

    // @PostMapping => POST /api/v1/medicos. Se usa para crear.
    @PostMapping
    @Operation(summary = "Guardado de medico", description = "Esta es la forma de guardar un medico")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Medico a crear", required = true,
            content = @Content(schema = @Schema(implementation = MedicoDTO.class))
    )
    // @Valid: valida el body contra las reglas del modelo (@NotBlank, @Pattern, etc.).
    // @RequestBody: convierte el JSON recibido en un objeto Medico.
    public ResponseEntity<Medico> save(@Valid @RequestBody Medico medico) {
        return ResponseEntity.ok(this.medicoService.save(medico));
    }

    // Busqueda por run en vez de por id => GET /api/v1/medicos/run/11111111-1
    @GetMapping("/run/{run}")
    @Operation(
            summary = "Busqueda de un medico por run",
            description = "Se devuelve un medico segun su run, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medico encontrado"),
            @ApiResponse(responseCode = "404", description = "Medico no se encuentra en la BD")
    })
    public ResponseEntity<Medico> findByRun(
            @Parameter(description = "Run del medico a buscar", required = true, example = "11111111-1")
            @PathVariable String run
    ) {
        return ResponseEntity.ok(this.medicoService.findByRun(run));
    }

    // @PutMapping => PUT /api/v1/medicos/{id}. Se usa para actualizar uno existente.
    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de medico", description = "Se actualizan los datos de un medico existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medico actualizado"),
            @ApiResponse(responseCode = "404", description = "Medico no se encuentra en la BD")
    })
    public ResponseEntity<Medico> update(
            @Parameter(description = "Id del medico a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Medico medico
    ) {
        return ResponseEntity.ok(this.medicoService.updateById(id, medico));
    }

    // @DeleteMapping => DELETE /api/v1/medicos/{id}.
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminacion de medico", description = "Se elimina un medico y sus atenciones asociadas")
    @ApiResponse(responseCode = "204", description = "Medico eliminado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del medico a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        this.medicoService.deleteById(id);
        // 204 No Content: se borro bien y no hay cuerpo que devolver.
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
