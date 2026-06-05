package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.DuplicateResourceException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.AddressDto;
import com.vitalink.platform.dto.patient.PatientRequest;
import com.vitalink.platform.dto.patient.PatientResponse;
import com.vitalink.platform.entity.Patient;
import com.vitalink.platform.entity.enums.Gender;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.PatientMapper;
import com.vitalink.platform.repository.PatientRepository;
import com.vitalink.platform.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientServiceImpl")
class PatientServiceImplTest {
    @Mock private PatientRepository patientRepository;

    private PatientServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PatientServiceImpl(patientRepository, new PatientMapper());
    }

    private PatientRequest validRequest() {
        PatientRequest request = new PatientRequest();
        request.setFullName("Maria Paciente");
        request.setCpf("12345678901");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setGender(Gender.FEMALE);
        request.setEmail("maria@medico.com");
        AddressDto address = AddressDto.builder()
                .street("Rua A").number("100").city("Sao Paulo").state("SP").zipCode("01000-000").build();
        request.setAddress(address);
        return request;
    }

    private Patient existing() {
        Patient p = Patient.builder()
                .fullName("Maria").cpf("12345678901").birthDate(LocalDate.of(1990, 1, 1))
                .status(RecordStatus.ACTIVE).build();
        p.setId(UUID.randomUUID());
        return p;
    }

    @Test
    @DisplayName("cria paciente com endereco")
    void shouldCreate() {
        when(patientRepository.existsByCpf("12345678901")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PatientResponse response = service.create(validRequest());

        assertThat(response.getCpf()).isEqualTo("12345678901");
        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getAddress().getCity()).isEqualTo("Sao Paulo");
    }

    @Test
    @DisplayName("falha ao criar com CPF duplicado")
    void shouldFailDuplicateCpf() {
        when(patientRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualiza paciente")
    void shouldUpdate() {
        Patient p = existing();
        PatientRequest request = validRequest();
        request.setFullName("Maria Atualizada");
        when(patientRepository.findById(p.getId())).thenReturn(Optional.of(p));

        PatientResponse response = service.update(p.getId(), request);

        assertThat(response.getFullName()).isEqualTo("Maria Atualizada");
    }

    @Test
    @DisplayName("falha ao atualizar para CPF de outro paciente")
    void shouldFailUpdateDuplicateCpf() {
        Patient p = existing();
        PatientRequest request = validRequest();
        request.setCpf("99999999999");
        when(patientRepository.findById(p.getId())).thenReturn(Optional.of(p));
        when(patientRepository.existsByCpf("99999999999")).thenReturn(true);

        assertThatThrownBy(() -> service.update(p.getId(), request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("getById lanca 404")
    void shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getById retorna paciente")
    void shouldGetById() {
        Patient p = existing();
        when(patientRepository.findById(p.getId())).thenReturn(Optional.of(p));

        assertThat(service.getById(p.getId()).getCpf()).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("lista pacientes paginados")
    void shouldList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> page = new PageImpl<>(List.of(existing()), pageable, 1);
        when(patientRepository.findAll(pageable)).thenReturn(page);

        PageResponse<PatientResponse> response = service.list(pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("inativa paciente")
    void shouldDeactivate() {
        Patient p = existing();
        when(patientRepository.findById(p.getId())).thenReturn(Optional.of(p));

        service.deactivate(p.getId());

        assertThat(p.getStatus()).isEqualTo(RecordStatus.INACTIVE);
    }

    @Test
    @DisplayName("reativa paciente inativado")
    void shouldActivate() {
        Patient p = existing();
        p.setStatus(RecordStatus.INACTIVE);
        when(patientRepository.findById(p.getId())).thenReturn(Optional.of(p));

        PatientResponse response = service.activate(p.getId());

        assertThat(p.getStatus()).isEqualTo(RecordStatus.ACTIVE);
        assertThat(response.getStatus()).isEqualTo(RecordStatus.ACTIVE);
    }
}
