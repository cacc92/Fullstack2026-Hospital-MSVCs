package com.hospital.msvc_atenciones.controllers;

import com.hospital.msvc_atenciones.models.Atencion;
import com.hospital.msvc_atenciones.services.AtencionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atenciones")
@Validated
public class AtencionController {

    @Autowired
    private AtencionService atencionService;

    @GetMapping
    public ResponseEntity<List<Atencion>> findAll(){
        return ResponseEntity.ok(atencionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Atencion> findById(@PathVariable Long id){
        return ResponseEntity.ok(atencionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Atencion> save(@Valid @RequestBody Atencion atencion){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(atencionService.save(atencion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Atencion> update(@PathVariable Long id, @Valid @RequestBody Atencion atencion){
        return ResponseEntity.ok(atencionService.updateById(atencion, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Atencion> delete(@PathVariable Long id){
        atencionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medico/{idMedico}")
    public ResponseEntity<List<Atencion>> findByMedicoId(@PathVariable Long idMedico){
        return ResponseEntity.ok(atencionService.findByMedicoId(idMedico));
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<Atencion>> findByPacienteId(@PathVariable Long idPaciente){
        return ResponseEntity.ok(atencionService.findByPacienteId(idPaciente));
    }



}
