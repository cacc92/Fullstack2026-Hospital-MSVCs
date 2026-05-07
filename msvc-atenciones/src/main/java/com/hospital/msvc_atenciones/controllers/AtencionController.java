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
    public ResponseEntity<List<Atencion>> getAllAtenciones() {
        return ResponseEntity.ok(atencionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Atencion> getAtencion(@PathVariable Long id){
        return ResponseEntity.ok(this.atencionService.findByID(id));
    }

    @PostMapping
    public ResponseEntity<Atencion> saveAtencion(@RequestBody Atencion atencion) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(atencionService.save(atencion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Atencion> updateById(@Valid @RequestBody Atencion atencion, @PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.atencionService.updateById(atencion, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id){
        this.atencionService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
