package com.hospital.msvc_pacientes.services;

import com.hospital.msvc_pacientes.clients.AtencionClient;
import com.hospital.msvc_pacientes.exceptions.PacienteException;
import com.hospital.msvc_pacientes.models.Paciente;
import com.hospital.msvc_pacientes.models.dtos.AtencionDTO;
import com.hospital.msvc_pacientes.repositories.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteServiceImpl implements PacienteService {
    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AtencionClient atencionClient;

    @Transactional(readOnly = true)
    @Override
    public List<Paciente> findAll() {
        return this.pacienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Paciente findById(Long id) {
        return this.pacienteRepository.findById(id).orElseThrow(
                () -> new PacienteException("Paciente no encontrado")
        );
    }

    @Transactional(readOnly = true)
    @Override
    public Paciente findByCorreo(String correo) {
        return this.pacienteRepository.findByCorreo(correo).orElseThrow(
                () -> new PacienteException("Paciente no encontrado")
        );
    }

    @Transactional(readOnly = true)
    @Override
    public Paciente findByRut(String rut) {
        return this.pacienteRepository.findByRut(rut).orElseThrow(
                () -> new PacienteException("Paciente no encontrado")
        );
    }

    @Transactional
    @Override
    public Paciente save(Paciente paciente) {
        if(this.pacienteRepository.findByCorreo(paciente.getCorreo()).isPresent()){
            throw new PacienteException("Paciente ya existe");
        }
        if(this.pacienteRepository.findByRut(paciente.getRut()).isPresent()){
            throw new PacienteException("Paciente ya existe");
        }
        return this.pacienteRepository.save(paciente);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        List<AtencionDTO> atenciones = this.atencionClient.getAtencionesByPacienteId(id);
        if(!atenciones.isEmpty()){
            for(AtencionDTO atencion: atenciones){
                this.atencionClient.deleteAtencionById(atencion.getAtencionId());
            }
        }
        this.pacienteRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Paciente updateById(Long id, Paciente paciente) {
        return this.pacienteRepository.findById(id).map(element-> {
            element.setNombres(paciente.getNombres());
            element.setApellidos(paciente.getApellidos());
            element.setFechaNacimiento(paciente.getFechaNacimiento());
            return this.pacienteRepository.save(element);
        }).orElseThrow(
                () -> new PacienteException("Paciente no encontrado")
        );
    }
}
