package com.hospital.msvc_atenciones.controllers;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController: cada metodo devuelve datos (JSON), no vistas HTML.
// @RequestMapping: prefijo comun de las rutas (version 1 de la API).
// @Validated: activa la validacion de los @Valid. @Tag: agrupa los endpoints en Swagger.
@RestController
@RequestMapping("/api/v1/atenciones")
@Validated
@Tag(name = "Atenciones V1", description = "Metodos CRUD para la gestión de atenciones")
public class AtencionController {

    @Autowired
    private AtencionService atencionService;

    // findAll devuelve DTOs "enriquecidos": el servicio pide a los msvc de medicos y
    // pacientes (via Feign) sus datos y los junta con la atencion en un solo objeto.
    @GetMapping
    @Operation(
            summary = "Listado de todas las atenciones",
            description = "Se devuelve una lista con las atenciones enriquecidas con datos de medico y paciente"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<AtencionDTO>> findAll(){
        return ResponseEntity.ok(atencionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de una atencion por id",
            description = "Se devuelve una atencion, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Atencion encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Atencion.class))),
            @ApiResponse(responseCode = "404", description = "Atencion no se encuentra en la BD")
    })
    public ResponseEntity<Atencion> findById(
            @Parameter(description = "Id de la atencion a buscar", required = true, example = "1")
            @PathVariable Long id
    ){
        return ResponseEntity.ok(atencionService.findById(id));
    }

    // POST /api/v1/atenciones => crear. @Valid valida el body; @RequestBody convierte el JSON.
    @PostMapping
    @Operation(summary = "Guardado de atencion", description = "Esta es la forma de guardar una atencion")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Atencion a crear", required = true,
            content = @Content(schema = @Schema(implementation = Atencion.class))
    )
    @ApiResponse(responseCode = "201", description = "Atencion creada")
    public ResponseEntity<Atencion> save(@Valid @RequestBody Atencion atencion){
        // El servicio valida que el medico y el paciente existan antes de guardar.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(atencionService.save(atencion));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de atencion", description = "Se actualizan los datos de una atencion existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atencion actualizada"),
            @ApiResponse(responseCode = "404", description = "Atencion no se encuentra en la BD")
    })
    public ResponseEntity<Atencion> update(
            @Parameter(description = "Id de la atencion a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Atencion atencion
    ){
        return ResponseEntity.ok(atencionService.updateById(atencion, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminacion de atencion", description = "Se elimina una atencion por su id")
    @ApiResponse(responseCode = "204", description = "Atencion eliminada")
    public ResponseEntity<Atencion> delete(
            @Parameter(description = "Id de la atencion a eliminar", required = true, example = "1")
            @PathVariable Long id
    ){
        atencionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medico/{idMedico}")
    @Operation(
            summary = "Listado de atenciones por medico",
            description = "Se devuelven las atenciones asociadas a un medico"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<Atencion>> findByMedicoId(
            @Parameter(description = "Id del medico", required = true, example = "1")
            @PathVariable Long idMedico
    ){
        return ResponseEntity.ok(atencionService.findByMedicoId(idMedico));
    }

    @GetMapping("/paciente/{idPaciente}")
    @Operation(
            summary = "Listado de atenciones por paciente",
            description = "Se devuelven las atenciones asociadas a un paciente"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<Atencion>> findByPacienteId(
            @Parameter(description = "Id del paciente", required = true, example = "1")
            @PathVariable Long idPaciente
    ){
        return ResponseEntity.ok(atencionService.findByPacienteId(idPaciente));
    }

}
