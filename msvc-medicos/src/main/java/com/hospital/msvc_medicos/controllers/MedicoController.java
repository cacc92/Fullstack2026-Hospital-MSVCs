package com.hospital.msvc_medicos.controllers;

import com.hospital.msvc_medicos.models.Medico;
import com.hospital.msvc_medicos.services.MedicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicos")
@Validated
public class MedicoController {

    @Autowired
    private MedicoService medicoService;

    @GetMapping
    public ResponseEntity<List<Medico>> findAll() {
        return ResponseEntity.ok(this.medicoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medico> findById(@PathVariable Long id) {
        return ResponseEntity.ok(this.medicoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Medico> save(@Valid @RequestBody Medico medico) {
        return ResponseEntity.ok(this.medicoService.save(medico));
    }
}
