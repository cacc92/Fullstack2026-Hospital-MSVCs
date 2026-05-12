package com.hospital.msvc_atenciones.services;

import com.hospital.msvc_atenciones.clients.MedicoClient;
import com.hospital.msvc_atenciones.clients.PacienteClient;
import com.hospital.msvc_atenciones.exceptions.AtencionException;
import com.hospital.msvc_atenciones.models.Atencion;
import com.hospital.msvc_atenciones.models.dtos.AtencionDTO;
import com.hospital.msvc_atenciones.models.dtos.MedicoDTO;
import com.hospital.msvc_atenciones.models.dtos.PacienteDTO;
import com.hospital.msvc_atenciones.models.dtos.PersonaDTO;
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
    public List<AtencionDTO> findAll() {
        return this.atencionRepository.findAll().stream().map(a->{
            AtencionDTO atencionDTO = new AtencionDTO();
            atencionDTO.setHoraAtencion(a.getHoraAtencion());
            atencionDTO.setId(a.getAtencionId());
            atencionDTO.setCosto(a.getCosto());
            atencionDTO.setComentario(a.getComentario());
            MedicoDTO medicoDTO = null;
            PacienteDTO pacienteDTO = null;
            try{
                medicoDTO = medicoClient.findById(a.getMedicoId());
                pacienteDTO = pacienteClient.getPacienteById(a.getPacienteId());
            }catch (FeignException e){
                throw new AtencionException(e.getMessage());
            }
            PersonaDTO medico = new PersonaDTO();
            medico.setId(a.getMedicoId());
            medico.setRut(medicoDTO.getRun());
            medico.setNombreCompleto(medicoDTO.getNombreCompleto());
            atencionDTO.setMedico(medico);

            PersonaDTO paciente = new PersonaDTO();
            paciente.setId(a.getPacienteId());
            paciente.setNombreCompleto(pacienteDTO.getNombres()+" "+pacienteDTO.getApellidos());
            paciente.setRut(pacienteDTO.getRut());
            atencionDTO.setPaciente(paciente);

            return atencionDTO;

        }).toList();
    }

    @Override
    public Atencion findById(Long id) {
        return this.atencionRepository.findById(id).orElseThrow(
                () -> new AtencionException("La atencion con id " + id + " no existe")
        );
    }

    @Override
    public Atencion save(Atencion atencion) {
        try {
            MedicoDTO medicoDTO = this.medicoClient.findById(atencion.getMedicoId());

        }catch (FeignException exception){
            throw new AtencionException("El medico con id "+ atencion.getMedicoId() +" no existe");
        }
        try {
            PacienteDTO pacienteDTO = this.pacienteClient.getPacienteById(atencion.getPacienteId());
        } catch (FeignException exception){
            throw new AtencionException("El paciente con "+atencion.getPacienteId()+" no existe");
        }
        return this.atencionRepository.save(atencion);

    }

    @Override
    public Atencion updateById(Atencion atencion, Long id) {
        return this.atencionRepository.findById(id).map(a->{
            a.setComentario(atencion.getComentario());
            a.setHoraAtencion(atencion.getHoraAtencion());
            try{
                MedicoDTO medicoDTO = this.medicoClient.findById(atencion.getMedicoId());
                a.setMedicoId(medicoDTO.getMedicoId());
            }catch (FeignException exception){
                throw new AtencionException("El medico con "+atencion.getMedicoId()+" no existe");
            }
            return atencionRepository.save(a);
        }).orElseThrow(
                () -> new AtencionException("La atencion con id "+ atencion.getAtencionId()+" no existe")
        );
    }

    @Override
    public void deleteById(Long id) {
        this.atencionRepository.deleteById(id);
    }

    @Override
    public List<Atencion> findByPacienteId(Long idPaciente) {
        return this.atencionRepository.findByPacienteId(idPaciente);
    }

    @Override
    public List<Atencion> findByMedicoId(Long idMedico) {
        return this.atencionRepository.findByMedicoId(idMedico);
    }
}
