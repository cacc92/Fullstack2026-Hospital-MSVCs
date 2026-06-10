package com.hospital.msvc_pacientes.controllers;

import com.hospital.msvc_pacientes.models.Paciente;
import com.hospital.msvc_pacientes.services.PacienteService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
@Validated
@Tag(name = "Pacientes V1", description = "Metodos CRUD para la gestión de pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @GetMapping
    @Operation(
            summary = "Listado de todos los pacientes",
            description = "Se devuelve una lista con los pacientes que se encuentran en la tabla pacientes de la DB"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<Paciente>> findAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pacienteService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un paciente por id",
            description = "Se devuelve un paciente, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Paciente encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Paciente.class))),
            @ApiResponse(responseCode = "404", description = "Paciente no se encuentra en la BD")
    })
    public ResponseEntity<Paciente> findById(
            @Parameter(description = "Id del paciente a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pacienteService.findById(id));
    }

    @GetMapping("/rut/{rut}")
    @Operation(
            summary = "Busqueda de un paciente por rut",
            description = "Se devuelve un paciente, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente no se encuentra en la BD")
    })
    public ResponseEntity<Paciente> findByRut(
            @Parameter(description = "Rut del paciente a buscar", required = true, example = "11111111-1")
            @PathVariable String rut
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pacienteService.findByRut(rut));
    }

    @GetMapping("/correo/{correo}")
    @Operation(
            summary = "Busqueda de un paciente por correo",
            description = "Se devuelve un paciente, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente no se encuentra en la BD")
    })
    public ResponseEntity<Paciente> findByCorreo(
            @Parameter(description = "Correo del paciente a buscar", required = true, example = "paciente@correo.cl")
            @PathVariable String correo
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pacienteService.findByCorreo(correo));
    }

    @PostMapping
    @Operation(summary = "Guardado de paciente", description = "Esta es la forma de guardar un paciente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Paciente a crear", required = true,
            content = @Content(schema = @Schema(implementation = Paciente.class))
    )
    @ApiResponse(responseCode = "201", description = "Paciente creado")
    public ResponseEntity<Paciente> save(@Valid @RequestBody Paciente paciente) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pacienteService.save(paciente));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de paciente", description = "Se actualizan los datos de un paciente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente actualizado"),
            @ApiResponse(responseCode = "404", description = "Paciente no se encuentra en la BD")
    })
    public ResponseEntity<Paciente> update(
            @Parameter(description = "Id del paciente a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Paciente paciente
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pacienteService.updateById(id, paciente));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminacion de paciente", description = "Se elimina un paciente y sus atenciones asociadas")
    @ApiResponse(responseCode = "204", description = "Paciente eliminado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del paciente a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        pacienteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
