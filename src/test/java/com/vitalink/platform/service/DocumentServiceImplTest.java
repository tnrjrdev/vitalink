package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.BusinessException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.document.DocumentResponse;
import com.vitalink.platform.entity.Document;
import com.vitalink.platform.entity.Patient;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.DocumentMapper;
import com.vitalink.platform.repository.DocumentRepository;
import com.vitalink.platform.service.impl.DocumentServiceImpl;
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
import org.springframework.mock.web.MockMultipartFile;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentServiceImpl")
class DocumentServiceImplTest {
    @Mock private DocumentRepository documentRepository;
    @Mock private PatientService patientService;
    @Mock private FileStorageService fileStorageService;

    private DocumentServiceImpl service;
    private Patient patient;

    @BeforeEach
    void setUp() throws Exception {
        service = new DocumentServiceImpl(documentRepository, patientService, fileStorageService, new DocumentMapper());
        patient = Patient.builder().fullName("Joao").status(RecordStatus.ACTIVE).build();
        patient.setId(UUID.randomUUID());
        lenient().when(fileStorageService.generatePresignedDownloadUrl(anyString()))
                .thenReturn(new URL("https://signed.example/download"));
    }

    private MockMultipartFile file() {
        return new MockMultipartFile("file", "exame.pdf", "application/pdf", new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("faz upload, persiste metadados e devolve URL assinada")
    void shouldUpload() {
        when(patientService.getEntityOrThrow(patient.getId())).thenReturn(patient);
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> {
            Document d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DocumentResponse response = service.upload(file(), patient.getId(), null, "Exame de sangue");

        assertThat(response.getFileName()).isEqualTo("exame.pdf");
        assertThat(response.getPatientId()).isEqualTo(patient.getId());
        assertThat(response.getSizeBytes()).isEqualTo(3L);
        assertThat(response.getDownloadUrl()).isEqualTo("https://signed.example/download");
        verify(fileStorageService).upload(anyString(), any(byte[].class), eq("application/pdf"));
    }

    @Test
    @DisplayName("falha quando o arquivo esta vazio")
    void shouldFailWhenEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[]{});

        assertThatThrownBy(() -> service.upload(empty, patient.getId(), null, null))
                .isInstanceOf(BusinessException.class);
        verify(fileStorageService, never()).upload(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("getById devolve metadados com URL assinada")
    void shouldGetById() {
        Document doc = existingDocument();
        when(documentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));

        DocumentResponse response = service.getById(doc.getId());

        assertThat(response.getId()).isEqualTo(doc.getId());
        assertThat(response.getDownloadUrl()).isEqualTo("https://signed.example/download");
    }

    @Test
    @DisplayName("getById lanca 404 quando nao existe")
    void shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("lista documentos por paciente")
    void shouldListByPatient() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Document> page = new PageImpl<>(List.of(existingDocument()), pageable, 1);
        when(documentRepository.findByPatientId(patient.getId(), pageable)).thenReturn(page);

        PageResponse<DocumentResponse> response = service.listByPatient(patient.getId(), pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getDownloadUrl resolve a URL assinada")
    void shouldGetDownloadUrl() {
        Document doc = existingDocument();
        when(documentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));

        assertThat(service.getDownloadUrl(doc.getId()).toString()).isEqualTo("https://signed.example/download");
    }

    @Test
    @DisplayName("delete remove do storage e do banco")
    void shouldDelete() {
        Document doc = existingDocument();
        when(documentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));

        service.delete(doc.getId());

        verify(fileStorageService).delete(doc.getStorageKey());
        verify(documentRepository).delete(doc);
    }

    private Document existingDocument() {
        Document doc = Document.builder()
                .patientId(patient.getId())
                .fileName("exame.pdf")
                .contentType("application/pdf")
                .sizeBytes(3L)
                .storageKey("patients/" + patient.getId() + "/abc-exame.pdf")
                .status(RecordStatus.ACTIVE)
                .build();
        doc.setId(UUID.randomUUID());
        return doc;
    }
}
