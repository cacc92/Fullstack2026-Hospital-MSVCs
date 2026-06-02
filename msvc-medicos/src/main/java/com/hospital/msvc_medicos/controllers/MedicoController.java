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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicos")
@Validated
@Tag(name="Medicos V1", description = "Metodos CRUD para la gestión de medicos")
public class MedicoController {

    @Autowired
    private MedicoService medicoService;

    @GetMapping
    @Operation(
            summary = "Listado de todos los medicos",
            description = "Se devuelve una lista con los medicos que se encuentran en la tabla medicos de la DB"

    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<Medico>> findAll() {
        return ResponseEntity.ok(this.medicoService.findAll());
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
    public ResponseEntity<Medico> findById(
            @Parameter(description = "Id del medico a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(this.medicoService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Guardado de medico", description = "Esta es la forma de guardar un medico")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Medico a crear", required = true,
            content = @Content(schema = @Schema(implementation = MedicoDTO.class))
    )
    public ResponseEntity<Medico> save(@Valid @RequestBody Medico medico) {
        return ResponseEntity.ok(this.medicoService.save(medico));
    }
}
