package com.hospital.msvc_medicos.services;


import com.hospital.msvc_medicos.exceptions.MedicoException;
import com.hospital.msvc_medicos.models.Medico;
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

@ExtendWith(MockitoExtension.class)
public class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private MedicoServiceImpl medicoService;

    private Medico medicoPrueba;
    private List<Medico> medicoList = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        this.medicoPrueba = new Medico();
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

    @Test
    @DisplayName("Debe listar todos los medicos")
    public void shouldBeListAllDoctors() {
        // Arrange
        List<Medico> medicos = this.medicoList;
        medicos.add(this.medicoPrueba);
        when(this.medicoRepository.findAll()).thenReturn(medicos);

        // ACT
        List<Medico> result = this.medicoService.findAll();

        // ASSERT
        assertThat(result).hasSize(101);
        assertThat(result).contains(medicoPrueba);
        verify(medicoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar un medico con un id inexistente")
    public void shouldNotFindMedicoById() {
        Long id = 9999L;
        when(this.medicoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            this.medicoService.findById(id);
        }).isInstanceOf(MedicoException.class)
                .hasMessage("Medico con id: "+id+" no encontrado");
        verify(medicoRepository, times(1)).findById(id);
    }

}
