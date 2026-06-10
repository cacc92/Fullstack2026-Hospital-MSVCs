package com.hospital.msvc_atenciones.services;

import com.hospital.msvc_atenciones.clients.MedicoClient;
import com.hospital.msvc_atenciones.clients.PacienteClient;
import com.hospital.msvc_atenciones.exceptions.AtencionException;
import com.hospital.msvc_atenciones.models.Atencion;
import com.hospital.msvc_atenciones.models.dtos.AtencionDTO;
import com.hospital.msvc_atenciones.models.dtos.MedicoDTO;
import com.hospital.msvc_atenciones.models.dtos.PacienteDTO;
import com.hospital.msvc_atenciones.repositories.AtencionRepository;
import feign.FeignException;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link AtencionServiceImpl}.
 *
 * <p>Se mockean el repositorio de atenciones y los clientes Feign de médicos y
 * pacientes para verificar tanto el enriquecimiento de datos como el manejo de
 * errores de comunicación entre microservicios.</p>
 */
// @ExtendWith(MockitoExtension.class): habilita Mockito. Pruebas unitarias: sin Spring, BD ni red real.
@ExtendWith(MockitoExtension.class)
public class AtencionServiceTest {

    @Mock
    private AtencionRepository atencionRepository;

    // Clientes Feign mockeados: simulamos las respuestas de los otros microservicios.
    @Mock
    private MedicoClient medicoClient;

    @Mock
    private PacienteClient pacienteClient;

    // @InjectMocks: crea el servicio real y le inyecta los 3 mocks de arriba.
    @InjectMocks
    private AtencionServiceImpl atencionService;

    private Atencion atencionPrueba;
    private List<Atencion> atencionList = new ArrayList<>();

    /**
     * Prepara una atención de prueba y una lista de 50 atenciones aleatorias
     * generadas con Datafaker antes de cada test.
     */
    @BeforeEach
    public void setUp() {
        this.atencionPrueba = new Atencion();
        this.atencionPrueba.setAtencionId(1L);
        this.atencionPrueba.setHoraAtencion(LocalDateTime.of(2026, 6, 10, 10, 30));
        this.atencionPrueba.setCosto(25000.0);
        this.atencionPrueba.setComentario("Control general");
        this.atencionPrueba.setMedicoId(10L);
        this.atencionPrueba.setPacienteId(20L);

        Faker faker = new Faker(Locale.of("es", "CL"));
        for (int i = 0; i < 50; i++) {
            Atencion atencion = new Atencion();
            atencion.setAtencionId((long) (i + 2));
            atencion.setHoraAtencion(LocalDateTime.of(2026, 1, 1, 9, 0));
            atencion.setCosto(faker.number().randomDouble(2, 10000, 90000));
            atencion.setComentario(faker.lorem().sentence());
            atencion.setMedicoId(10L);
            atencion.setPacienteId(20L);
            atencionList.add(atencion);
        }
    }

    /** Verifica el listado enriquecido con datos de médico y paciente. */
    @Test
    @DisplayName("Debe listar todas las atenciones enriquecidas")
    public void shouldListAllAtenciones() {
        // Arrange: el repo devuelve 1 atencion, y simulamos las respuestas de los msvc
        // de medicos y pacientes para que el servicio pueda "enriquecer" el DTO.
        when(this.atencionRepository.findAll()).thenReturn(List.of(this.atencionPrueba));

        MedicoDTO medicoDTO = new MedicoDTO();
        medicoDTO.setMedicoId(10L);
        medicoDTO.setRun("11111111-1");
        medicoDTO.setNombreCompleto("Dra. Ana Contreras");
        when(this.medicoClient.findById(10L)).thenReturn(medicoDTO);

        PacienteDTO pacienteDTO = new PacienteDTO();
        pacienteDTO.setPacienteId(20L);
        pacienteDTO.setRut("22222222-2");
        pacienteDTO.setNombres("Pedro");
        pacienteDTO.setApellidos("Soto");
        when(this.pacienteClient.getPacienteById(20L)).thenReturn(pacienteDTO);

        List<AtencionDTO> result = this.atencionService.findAll();

        assertThat(result).hasSize(1);
        AtencionDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getMedico().getNombreCompleto()).isEqualTo("Dra. Ana Contreras");
        assertThat(dto.getPaciente().getNombreCompleto()).isEqualTo("Pedro Soto");
        verify(medicoClient, times(1)).findById(10L);
        verify(pacienteClient, times(1)).getPacienteById(20L);
    }

    /** Verifica la búsqueda exitosa de una atención por id. */
    @Test
    @DisplayName("Debe buscar una atencion por su id")
    public void shouldFindAtencionById() {
        Long id = 1L;
        when(this.atencionRepository.findById(id)).thenReturn(Optional.of(this.atencionPrueba));

        Atencion result = this.atencionService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getComentario()).isEqualTo("Control general");
        verify(atencionRepository, times(1)).findById(id);
    }

    /** Verifica que se lanza excepción al buscar un id inexistente. */
    @Test
    @DisplayName("Debe lanzar excepcion al buscar una atencion con id inexistente")
    public void shouldNotFindAtencionById() {
        Long id = 9999L;
        when(this.atencionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.atencionService.findById(id))
                .isInstanceOf(AtencionException.class)
                .hasMessage("La atencion con id " + id + " no existe");
        verify(atencionRepository, times(1)).findById(id);
    }

    /** Verifica el guardado de una atención con médico y paciente válidos. */
    @Test
    @DisplayName("Debe guardar una atencion valida")
    public void shouldSaveAtencion() {
        when(this.medicoClient.findById(10L)).thenReturn(new MedicoDTO());
        when(this.pacienteClient.getPacienteById(20L)).thenReturn(new PacienteDTO());
        when(this.atencionRepository.save(this.atencionPrueba)).thenReturn(this.atencionPrueba);

        Atencion result = this.atencionService.save(this.atencionPrueba);

        assertThat(result).isNotNull();
        assertThat(result.getMedicoId()).isEqualTo(10L);
        verify(atencionRepository, times(1)).save(this.atencionPrueba);
    }

    /** Verifica que no se guarda una atención cuando el médico no existe. */
    @Test
    @DisplayName("Debe lanzar excepcion al guardar con medico inexistente")
    public void shouldNotSaveAtencionWhenMedicoNotExists() {
        // Simulamos que el msvc de medicos responde con error (FeignException = fallo HTTP).
        // El servicio debe traducirlo a su propia AtencionException y no guardar nada.
        when(this.medicoClient.findById(10L)).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> this.atencionService.save(this.atencionPrueba))
                .isInstanceOf(AtencionException.class)
                .hasMessage("El medico con id " + this.atencionPrueba.getMedicoId() + " no existe");
        verify(atencionRepository, never()).save(any(Atencion.class));
    }

    /** Verifica que no se guarda una atención cuando el paciente no existe. */
    @Test
    @DisplayName("Debe lanzar excepcion al guardar con paciente inexistente")
    public void shouldNotSaveAtencionWhenPacienteNotExists() {
        when(this.medicoClient.findById(10L)).thenReturn(new MedicoDTO());
        when(this.pacienteClient.getPacienteById(20L)).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> this.atencionService.save(this.atencionPrueba))
                .isInstanceOf(AtencionException.class)
                .hasMessage("El paciente con " + this.atencionPrueba.getPacienteId() + " no existe");
        verify(atencionRepository, never()).save(any(Atencion.class));
    }

    /** Verifica la actualización de una atención existente. */
    @Test
    @DisplayName("Debe actualizar una atencion existente")
    public void shouldUpdateAtencion() {
        Long id = 1L;
        Atencion cambios = new Atencion();
        cambios.setComentario("Comentario actualizado");
        cambios.setHoraAtencion(LocalDateTime.of(2026, 7, 1, 12, 0));
        cambios.setMedicoId(10L);

        MedicoDTO medicoDTO = new MedicoDTO();
        medicoDTO.setMedicoId(10L);

        when(this.atencionRepository.findById(id)).thenReturn(Optional.of(this.atencionPrueba));
        when(this.medicoClient.findById(10L)).thenReturn(medicoDTO);
        when(this.atencionRepository.save(any(Atencion.class))).thenAnswer(inv -> inv.getArgument(0));

        Atencion result = this.atencionService.updateById(cambios, id);

        assertThat(result.getComentario()).isEqualTo("Comentario actualizado");
        assertThat(result.getMedicoId()).isEqualTo(10L);
        verify(atencionRepository, times(1)).findById(id);
        verify(atencionRepository, times(1)).save(this.atencionPrueba);
    }

    /** Verifica que se lanza excepción al actualizar una atención inexistente. */
    @Test
    @DisplayName("Debe lanzar excepcion al actualizar una atencion inexistente")
    public void shouldNotUpdateAtencionWhenNotExists() {
        Long id = 9999L;
        when(this.atencionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.atencionService.updateById(this.atencionPrueba, id))
                .isInstanceOf(AtencionException.class)
                .hasMessage("La atencion con id " + this.atencionPrueba.getAtencionId() + " no existe");
        verify(atencionRepository, times(1)).findById(id);
        verify(atencionRepository, never()).save(any(Atencion.class));
    }

    /** Verifica la eliminación de una atención por id. */
    @Test
    @DisplayName("Debe eliminar una atencion por su id")
    public void shouldDeleteAtencion() {
        Long id = 1L;

        this.atencionService.deleteById(id);

        verify(atencionRepository, times(1)).deleteById(id);
    }

    /** Verifica el listado de atenciones por médico. */
    @Test
    @DisplayName("Debe listar las atenciones por medico")
    public void shouldFindByMedicoId() {
        Long idMedico = 10L;
        when(this.atencionRepository.findByMedicoId(idMedico)).thenReturn(this.atencionList);

        List<Atencion> result = this.atencionService.findByMedicoId(idMedico);

        assertThat(result).hasSize(50);
        verify(atencionRepository, times(1)).findByMedicoId(idMedico);
    }

    /** Verifica el listado de atenciones por paciente. */
    @Test
    @DisplayName("Debe listar las atenciones por paciente")
    public void shouldFindByPacienteId() {
        Long idPaciente = 20L;
        when(this.atencionRepository.findByPacienteId(idPaciente)).thenReturn(this.atencionList);

        List<Atencion> result = this.atencionService.findByPacienteId(idPaciente);

        assertThat(result).hasSize(50);
        verify(atencionRepository, times(1)).findByPacienteId(idPaciente);
    }
}
