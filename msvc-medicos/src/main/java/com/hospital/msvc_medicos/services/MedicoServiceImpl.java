package com.hospital.msvc_medicos.services;

import com.hospital.msvc_medicos.clients.AtencionClient;
import com.hospital.msvc_medicos.exceptions.MedicoException;
import com.hospital.msvc_medicos.models.Medico;
import com.hospital.msvc_medicos.models.dtos.AtencionDTO;
import com.hospital.msvc_medicos.repositories.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoServiceImpl implements MedicoService {
    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private AtencionClient atencionClient;

    @Transactional(readOnly = true)
    @Override
    public List<Medico> findAll() {
        return this.medicoRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Medico findById(Long id) {
        return this.medicoRepository.findById(id).orElseThrow(
                () -> new MedicoException("Medico con id: "+id+" no encontrado")
        );
    }

    @Transactional(readOnly = true)
    @Override
    public Medico findByRun(String run) {
        return this.medicoRepository.findByRun(run).orElseThrow(
                () -> new MedicoException("Medico con rut"+run+" no encontrado")
        );
    }

    @Transactional
    @Override
    public Medico save(Medico medico) {
        if (this.medicoRepository.findByRun(medico.getRun()).isPresent()){
            throw new MedicoException("Medico con rut: "+medico.getRun()+" ya existente");
        }
        return this.medicoRepository.save(medico);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        List<AtencionDTO> atenciones = this.atencionClient.getAtencionesByMedicoId(id);
        if (!atenciones.isEmpty()){
            for (AtencionDTO atencion : atenciones){
                this.atencionClient.deleteAtencionById(atencion.getAtencionId());
            }
        }
        this.medicoRepository.deleteById(id);
    }

    @Override
    public Medico updateById(Long id, Medico medico) {
        return this.medicoRepository.findById(id).map(element -> {
            // element.setRun(medico.getRun());
            element.setJefeTurno(medico.getJefeTurno());
            element.setNombreCompleto(medico.getNombreCompleto());
            return this.medicoRepository.save(element);

        }).orElseThrow(
                () -> new MedicoException("El medico con id: "+id+" no existe")
        );
    }

}
