package com.hospital.msvc_atenciones.services;

import com.hospital.msvc_atenciones.clients.MedicoClient;
import com.hospital.msvc_atenciones.clients.PacienteClient;
import com.hospital.msvc_atenciones.exceptions.AtencionException;
import com.hospital.msvc_atenciones.models.Atencion;
import com.hospital.msvc_atenciones.models.dtos.MedicoDTO;
import com.hospital.msvc_atenciones.models.dtos.PacienteDTO;
import com.hospital.msvc_atenciones.repositories.AtencionRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtencionServiceImpl implements AtencionService {

    @Autowired
    private AtencionRepository atencionRepository;

    @Autowired
    private MedicoClient medicoClient;

    @Autowired
    private PacienteClient pacienteClient;

    @Override
    public List<Atencion> findAll() {
        return atencionRepository.findAll();
    }

    @Override
    public Atencion findByID(Long id) {
        return this.atencionRepository.findById(id).orElseThrow(
                () -> new AtencionException("La atencion con id "+id+" no existe")
        );
    }

    @Override
    public Atencion save(Atencion atencion) {
        try {
            MedicoDTO medico = this.medicoClient.getMedicoById(atencion.getMedicoId());
        } catch (FeignException exception){
            throw new AtencionException("El medico no existe");
        }
        try {
            PacienteDTO paciente = this.pacienteClient.getPacienteById(atencion.getPacienteId());
        }catch (FeignException exception){
            throw new AtencionException("El paciente no existe");
        }
        return atencionRepository.save(atencion);

    }

    @Override
    public Atencion updateById(Atencion atencion, Long id) {
        return this.atencionRepository.findById(id).map(a->{
            a.setComentario(atencion.getComentario());
            a.setHoraAtencion(atencion.getHoraAtencion());
            try{
                MedicoDTO medico = this.medicoClient.getMedicoById(atencion.getMedicoId());
                a.setMedicoId(medico.getMedicoId());
            }catch (FeignException exception){
                throw new AtencionException("El medico con id "+id+" no existe");
            }
            a.setCosto(atencion.getCosto());
            return this.atencionRepository.save(a);

        }).orElseThrow(
                () -> new AtencionException("La atención con id: "+id+" no existe")
        );
    }

    @Override
    public void deleteById(Long id) {
        this.atencionRepository.deleteById(id);
    }
}
