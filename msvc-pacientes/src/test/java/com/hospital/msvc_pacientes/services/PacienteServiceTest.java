package com.hospital.msvc_pacientes.services;

import com.hospital.msvc_pacientes.clients.AtencionClient;
import com.hospital.msvc_pacientes.exceptions.PacienteException;
import com.hospital.msvc_pacientes.models.Paciente;
import com.hospital.msvc_pacientes.models.dtos.AtencionDTO;
import com.hospital.msvc_pacientes.repositories.PacienteRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link PacienteServiceImpl}.
 *
 * <p>Se mockean el repositorio de pacientes y el cliente Feign de atenciones
 * para validar la lógica de negocio de forma aislada (sin base de datos ni red).</p>
 */
// @ExtendWith(MockitoExtension.class): habilita Mockito. Pruebas unitarias: sin Spring ni BD real.
@ExtendWith(MockitoExtension.class)
public class PacienteServiceTest {

    // @Mock: objetos falsos. Decidimos su comportamiento con when(...).
    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private AtencionClient atencionClient;

    // @InjectMocks: crea el servicio real y le inyecta los mocks de arriba.
    @InjectMocks
    private PacienteServiceImpl pacienteService;

    private Paciente pacientePrueba;
    private List<Paciente> pacienteList = new ArrayList<>();

    /**
     * Prepara un paciente de prueba y una lista de 100 pacientes aleatorios
     * generados con Datafaker antes de cada test.
     */
    @BeforeEach
    public void setUp() {
        this.pacientePrueba = new Paciente();
        this.pacientePrueba.setPacienteId(1L);
        this.pacientePrueba.setRut("11111111-1");
        this.pacientePrueba.setNombres("Ana");
        this.pacientePrueba.setApellidos("Contreras");
        this.pacientePrueba.setCorreo("ana.contreras@correo.cl");
        this.pacientePrueba.setFechaNacimiento(LocalDate.of(1990, 5, 20));

        Faker faker = new Faker(Locale.of("es", "CL"));
        for (int i = 0; i < 100; i++) {
            Paciente paciente = new Paciente();
            String numeroStr = faker.idNumber().valid().replace("-", "");
            String ultimo = numeroStr.substring(numeroStr.length() - 1);
            String restante = numeroStr.substring(0, numeroStr.length() - 1);

            paciente.setRut(restante + "-" + ultimo);
            paciente.setNombres(faker.name().firstName());
            paciente.setApellidos(faker.name().lastName());
            paciente.setCorreo(faker.internet().emailAddress());
            paciente.setFechaNacimiento(LocalDate.of(1985, 1, 1));

            pacienteList.add(paciente);
        }
    }

    /** Verifica que se listan todos los pacientes del repositorio. */
    @Test
    @DisplayName("Debe listar todos los pacientes")
    public void shouldListAllPacientes() {
        List<Paciente> pacientes = this.pacienteList;
        pacientes.add(this.pacientePrueba);
        when(this.pacienteRepository.findAll()).thenReturn(pacientes);

        List<Paciente> result = this.pacienteService.findAll();

        assertThat(result).hasSize(101);
        assertThat(result).contains(pacientePrueba);
        verify(pacienteRepository, times(1)).findAll();
    }

    /** Verifica la búsqueda exitosa de un paciente por id. */
    @Test
    @DisplayName("Debe buscar un paciente por su id")
    public void shouldFindPacienteById() {
        Long id = 1L;
        when(this.pacienteRepository.findById(id)).thenReturn(Optional.of(this.pacientePrueba));

        Paciente result = this.pacienteService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getRut()).isEqualTo("11111111-1");
        verify(pacienteRepository, times(1)).findById(id);
    }

    /** Verifica que se lanza excepción al buscar un id inexistente. */
    @Test
    @DisplayName("Debe lanzar excepcion al buscar un paciente con id inexistente")
    public void shouldNotFindPacienteById() {
        Long id = 9999L;
        when(this.pacienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.pacienteService.findById(id))
                .isInstanceOf(PacienteException.class)
                .hasMessage("Paciente no encontrado");
        verify(pacienteRepository, times(1)).findById(id);
    }

    /** Verifica la búsqueda exitosa de un paciente por rut. */
    @Test
    @DisplayName("Debe buscar un paciente por su rut")
    public void shouldFindPacienteByRut() {
        String rut = "11111111-1";
        when(this.pacienteRepository.findByRut(rut)).thenReturn(Optional.of(this.pacientePrueba));

        Paciente result = this.pacienteService.findByRut(rut);

        assertThat(result).isNotNull();
        assertThat(result.getRut()).isEqualTo(rut);
        verify(pacienteRepository, times(1)).findByRut(rut);
    }

    /** Verifica la búsqueda exitosa de un paciente por correo. */
    @Test
    @DisplayName("Debe buscar un paciente por su correo")
    public void shouldFindPacienteByCorreo() {
        String correo = "ana.contreras@correo.cl";
        when(this.pacienteRepository.findByCorreo(correo)).thenReturn(Optional.of(this.pacientePrueba));

        Paciente result = this.pacienteService.findByCorreo(correo);

        assertThat(result).isNotNull();
        assertThat(result.getCorreo()).isEqualTo(correo);
        verify(pacienteRepository, times(1)).findByCorreo(correo);
    }

    /** Verifica el guardado de un paciente nuevo (correo y rut no existentes). */
    @Test
    @DisplayName("Debe guardar un paciente nuevo")
    public void shouldSavePaciente() {
        // El servicio valida que no exista el correo NI el rut antes de guardar: ambos vacios = ok.
        when(this.pacienteRepository.findByCorreo(this.pacientePrueba.getCorreo())).thenReturn(Optional.empty());
        when(this.pacienteRepository.findByRut(this.pacientePrueba.getRut())).thenReturn(Optional.empty());
        when(this.pacienteRepository.save(this.pacientePrueba)).thenReturn(this.pacientePrueba);

        Paciente result = this.pacienteService.save(this.pacientePrueba);

        assertThat(result).isNotNull();
        assertThat(result.getCorreo()).isEqualTo("ana.contreras@correo.cl");
        verify(pacienteRepository, times(1)).save(this.pacientePrueba);
    }

    /** Verifica que no se guarda un paciente cuando el correo ya existe. */
    @Test
    @DisplayName("Debe lanzar excepcion al guardar un paciente con correo existente")
    public void shouldNotSavePacienteWhenCorreoExists() {
        // El correo ya existe => el servicio debe lanzar excepcion y NO llamar a save.
        when(this.pacienteRepository.findByCorreo(this.pacientePrueba.getCorreo()))
                .thenReturn(Optional.of(this.pacientePrueba));

        assertThatThrownBy(() -> this.pacienteService.save(this.pacientePrueba))
                .isInstanceOf(PacienteException.class)
                .hasMessage("Paciente ya existe");
        // never(): confirmamos que jamas se intento guardar.
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    /** Verifica que no se guarda un paciente cuando el rut ya existe. */
    @Test
    @DisplayName("Debe lanzar excepcion al guardar un paciente con rut existente")
    public void shouldNotSavePacienteWhenRutExists() {
        when(this.pacienteRepository.findByCorreo(this.pacientePrueba.getCorreo())).thenReturn(Optional.empty());
        when(this.pacienteRepository.findByRut(this.pacientePrueba.getRut()))
                .thenReturn(Optional.of(this.pacientePrueba));

        assertThatThrownBy(() -> this.pacienteService.save(this.pacientePrueba))
                .isInstanceOf(PacienteException.class)
                .hasMessage("Paciente ya existe");
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    /** Verifica la actualización de un paciente existente. */
    @Test
    @DisplayName("Debe actualizar un paciente existente")
    public void shouldUpdatePacienteById() {
        Long id = 1L;
        Paciente cambios = new Paciente();
        cambios.setNombres("Gregory");
        cambios.setApellidos("House");
        cambios.setFechaNacimiento(LocalDate.of(1980, 3, 15));

        when(this.pacienteRepository.findById(id)).thenReturn(Optional.of(this.pacientePrueba));
        when(this.pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        Paciente result = this.pacienteService.updateById(id, cambios);

        assertThat(result.getNombres()).isEqualTo("Gregory");
        assertThat(result.getApellidos()).isEqualTo("House");
        assertThat(result.getFechaNacimiento()).isEqualTo(LocalDate.of(1980, 3, 15));
        verify(pacienteRepository, times(1)).findById(id);
        verify(pacienteRepository, times(1)).save(this.pacientePrueba);
    }

    /** Verifica que se lanza excepción al actualizar un paciente inexistente. */
    @Test
    @DisplayName("Debe lanzar excepcion al actualizar un paciente inexistente")
    public void shouldNotUpdatePacienteWhenNotExists() {
        Long id = 9999L;
        when(this.pacienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.pacienteService.updateById(id, this.pacientePrueba))
                .isInstanceOf(PacienteException.class)
                .hasMessage("Paciente no encontrado");
        verify(pacienteRepository, times(1)).findById(id);
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    /** Verifica la eliminación de un paciente sin atenciones asociadas. */
    @Test
    @DisplayName("Debe eliminar un paciente sin atenciones asociadas")
    public void shouldDeletePacienteById() {
        Long id = 1L;
        when(this.atencionClient.getAtencionByIdPaciente(id)).thenReturn(new ArrayList<>());

        this.pacienteService.deleteById(id);

        verify(atencionClient, times(1)).getAtencionByIdPaciente(id);
        verify(atencionClient, never()).deleteAtencionById(anyLong());
        verify(pacienteRepository, times(1)).deleteById(id);
    }

    /** Verifica que al eliminar un paciente también se eliminan sus atenciones. */
    @Test
    @DisplayName("Debe eliminar un paciente junto con sus atenciones asociadas")
    public void shouldDeletePacienteByIdWithAtenciones() {
        Long id = 1L;
        AtencionDTO atencion = new AtencionDTO();
        atencion.setAtencionId(50L);
        atencion.setPacienteId(id);
        when(this.atencionClient.getAtencionByIdPaciente(id)).thenReturn(List.of(atencion));

        this.pacienteService.deleteById(id);

        verify(atencionClient, times(1)).getAtencionByIdPaciente(id);
        verify(atencionClient, times(1)).deleteAtencionById(50L);
        verify(pacienteRepository, times(1)).deleteById(id);
    }
}
