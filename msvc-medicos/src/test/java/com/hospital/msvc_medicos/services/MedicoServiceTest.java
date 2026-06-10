package com.hospital.msvc_medicos.services;


import com.hospital.msvc_medicos.clients.AtencionClient;
import com.hospital.msvc_medicos.exceptions.MedicoException;
import com.hospital.msvc_medicos.models.Medico;
import com.hospital.msvc_medicos.models.dtos.AtencionDTO;
import com.hospital.msvc_medicos.repositories.MedicoRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): habilita Mockito para crear los mocks de abajo.
// Son pruebas unitarias: NO levantan Spring ni la base de datos, todo se simula.
@ExtendWith(MockitoExtension.class)
public class MedicoServiceTest {

    // @Mock: objeto falso. No accede a la BD; nosotros decidimos que devuelve con when(...).
    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private AtencionClient atencionClient;

    // @InjectMocks: crea el servicio real y le inyecta los @Mock de arriba.
    @InjectMocks
    private MedicoServiceImpl medicoService;

    private Medico medicoPrueba;
    private List<Medico> medicoList = new ArrayList<>();

    // @BeforeEach se ejecuta antes de CADA test para dejar los datos en un estado conocido.
    @BeforeEach
    public void setUp() {
        this.medicoPrueba = new Medico();
        this.medicoPrueba.setMedicoId(1L);
        this.medicoPrueba.setRun("11111111-1");
        this.medicoPrueba.setNombreCompleto("Dra. Ana Contreras");
        this.medicoPrueba.setJefeTurno(true);

        Faker faker = new Faker(Locale.of("es","CL"));
        for (int i = 0; i < 100; i++) {
            Medico medico = new Medico();
            // "XX-XX-XX-XX"
            String numeroStr = faker.idNumber().valid().replace("-", "");
            String ultimo = numeroStr.substring(numeroStr.length()-1);
            String restante = numeroStr.substring(0,numeroStr.length()-1);

            medico.setRun(restante + "-" + ultimo);
            medico.setNombreCompleto(faker.name().fullName());

            medicoList.add(medico);
        }
    }

    // Patron AAA: Arrange (preparar) -> Act (ejecutar) -> Assert (verificar).
    @Test
    @DisplayName("Debe listar todos los medicos")
    public void shouldBeListAllDoctors() {
        // Arrange: definimos que devuelve el mock cuando se llame findAll().
        List<Medico> medicos = this.medicoList;
        medicos.add(this.medicoPrueba);
        when(this.medicoRepository.findAll()).thenReturn(medicos);

        // ACT: llamamos al metodo real del servicio.
        List<Medico> result = this.medicoService.findAll();

        // ASSERT: comprobamos el resultado y que el repo se uso 1 vez.
        assertThat(result).hasSize(101);
        assertThat(result).contains(medicoPrueba);
        verify(medicoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar un medico por su id")
    public void shouldFindMedicoById() {
        // Arrange
        Long id = 1L;
        when(this.medicoRepository.findById(id)).thenReturn(Optional.of(this.medicoPrueba));

        // Act
        Medico result = this.medicoService.findById(id);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRun()).isEqualTo("11111111-1");
        assertThat(result.getNombreCompleto()).isEqualTo("Dra. Ana Contreras");
        verify(medicoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe buscar un medico con un id inexistente")
    public void shouldNotFindMedicoById() {
        Long id = 9999L;
        // El repo "no encuentra" nada (Optional vacio), asi el servicio debe lanzar excepcion.
        when(this.medicoRepository.findById(id)).thenReturn(Optional.empty());

        // assertThatThrownBy verifica que se lance la excepcion esperada y con el mensaje correcto.
        assertThatThrownBy(() -> {
            this.medicoService.findById(id);
        }).isInstanceOf(MedicoException.class)
                .hasMessage("Medico con id: "+id+" no encontrado");
        verify(medicoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe buscar un medico por su run")
    public void shouldFindMedicoByRun() {
        // Arrange
        String run = "11111111-1";
        when(this.medicoRepository.findByRun(run)).thenReturn(Optional.of(this.medicoPrueba));

        // Act
        Medico result = this.medicoService.findByRun(run);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRun()).isEqualTo(run);
        verify(medicoRepository, times(1)).findByRun(run);
    }

    @Test
    @DisplayName("Debe lanzar excepcion al buscar un run inexistente")
    public void shouldNotFindMedicoByRun() {
        String run = "99999999-9";
        when(this.medicoRepository.findByRun(run)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            this.medicoService.findByRun(run);
        }).isInstanceOf(MedicoException.class)
                .hasMessage("Medico con rut"+run+" no encontrado");
        verify(medicoRepository, times(1)).findByRun(run);
    }

    @Test
    @DisplayName("Debe guardar un medico nuevo")
    public void shouldSaveMedico() {
        // Arrange
        when(this.medicoRepository.findByRun(this.medicoPrueba.getRun())).thenReturn(Optional.empty());
        when(this.medicoRepository.save(this.medicoPrueba)).thenReturn(this.medicoPrueba);

        // Act
        Medico result = this.medicoService.save(this.medicoPrueba);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRun()).isEqualTo("11111111-1");
        verify(medicoRepository, times(1)).findByRun(this.medicoPrueba.getRun());
        verify(medicoRepository, times(1)).save(this.medicoPrueba);
    }

    @Test
    @DisplayName("Debe lanzar excepcion al guardar un medico con run existente")
    public void shouldNotSaveMedicoWhenRunExists() {
        // Arrange
        when(this.medicoRepository.findByRun(this.medicoPrueba.getRun()))
                .thenReturn(Optional.of(this.medicoPrueba));

        // Act + Assert
        assertThatThrownBy(() -> {
            this.medicoService.save(this.medicoPrueba);
        }).isInstanceOf(MedicoException.class)
                .hasMessage("Medico con rut: "+this.medicoPrueba.getRun()+" ya existente");
        verify(medicoRepository, times(1)).findByRun(this.medicoPrueba.getRun());
        verify(medicoRepository, never()).save(any(Medico.class));
    }

    @Test
    @DisplayName("Debe actualizar un medico existente")
    public void shouldUpdateMedicoById() {
        // Arrange
        Long id = 1L;
        Medico cambios = new Medico();
        cambios.setNombreCompleto("Dr. Gregory House");
        cambios.setJefeTurno(false);

        when(this.medicoRepository.findById(id)).thenReturn(Optional.of(this.medicoPrueba));
        when(this.medicoRepository.save(any(Medico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Medico result = this.medicoService.updateById(id, cambios);

        // Assert
        assertThat(result.getNombreCompleto()).isEqualTo("Dr. Gregory House");
        assertThat(result.getJefeTurno()).isFalse();
        verify(medicoRepository, times(1)).findById(id);
        verify(medicoRepository, times(1)).save(this.medicoPrueba);
    }

    @Test
    @DisplayName("Debe lanzar excepcion al actualizar un medico inexistente")
    public void shouldNotUpdateMedicoWhenNotExists() {
        // Arrange
        Long id = 9999L;
        when(this.medicoRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> {
            this.medicoService.updateById(id, this.medicoPrueba);
        }).isInstanceOf(MedicoException.class)
                .hasMessage("El medico con id: "+id+" no existe");
        verify(medicoRepository, times(1)).findById(id);
        verify(medicoRepository, never()).save(any(Medico.class));
    }

    @Test
    @DisplayName("Debe eliminar un medico sin atenciones asociadas")
    public void shouldDeleteMedicoById() {
        // Arrange
        Long id = 1L;
        when(this.atencionClient.getAtencionesByIdMedico(id)).thenReturn(new ArrayList<>());

        // Act
        this.medicoService.deleteById(id);

        // Assert
        verify(atencionClient, times(1)).getAtencionesByIdMedico(id);
        verify(atencionClient, never()).deleteAtencionById(anyLong());
        verify(medicoRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Debe eliminar un medico junto con sus atenciones asociadas")
    public void shouldDeleteMedicoByIdWithAtenciones() {
        // Arrange: simulamos que el otro microservicio (via Feign) devuelve 1 atencion.
        Long id = 1L;
        AtencionDTO atencion = new AtencionDTO();
        atencion.setAtencionId(50L);
        atencion.setMedicoId(id);
        when(this.atencionClient.getAtencionesByIdMedico(id)).thenReturn(List.of(atencion));

        // Act
        this.medicoService.deleteById(id);

        // Assert: primero debe borrar la atencion asociada y luego el medico.
        verify(atencionClient, times(1)).getAtencionesByIdMedico(id);
        verify(atencionClient, times(1)).deleteAtencionById(50L);
        verify(medicoRepository, times(1)).deleteById(id);
    }

}
