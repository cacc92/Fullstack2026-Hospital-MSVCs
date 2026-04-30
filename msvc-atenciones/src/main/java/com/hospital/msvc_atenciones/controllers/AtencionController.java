package com.hospital.msvc_atenciones.controllers;

import com.hospital.msvc_atenciones.models.Atencion;
import com.hospital.msvc_atenciones.services.AtencionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atenciones")
public class AtencionController {
    @Autowired
    private AtencionService atencionService;

    @GetMapping
    public ResponseEntity<List<Atencion>> getAllAtenciones() {
        return ResponseEntity.ok(atencionService.findAll());
    }

    @PostMapping
    public ResponseEntity<Atencion> saveAtencion(@RequestBody Atencion atencion) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(atencionService.save(atencion));
    }
}
