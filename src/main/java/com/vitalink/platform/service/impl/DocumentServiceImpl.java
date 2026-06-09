package com.vitalink.platform.service.impl;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.BusinessException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.common.exception.StorageException;
import com.vitalink.platform.dto.document.DocumentResponse;
import com.vitalink.platform.entity.Document;
import com.vitalink.platform.entity.Patient;
import com.vitalink.platform.mapper.DocumentMapper;
import com.vitalink.platform.repository.DocumentRepository;
import com.vitalink.platform.service.DocumentService;
import com.vitalink.platform.service.FileStorageService;
import com.vitalink.platform.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final PatientService patientService;
    private final FileStorageService fileStorageService;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public DocumentResponse upload(MultipartFile file, UUID patientId, UUID appointmentId, String description) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo ausente ou vazio");
        }
        Patient patient = patientService.getEntityOrThrow(patientId);

        String originalName = sanitize(file.getOriginalFilename());
        String storageKey = buildKey(patient.getId(), originalName);

        try {
            fileStorageService.upload(storageKey, file.getBytes(), file.getContentType());
        } catch (IOException ex) {
            throw new StorageException("Falha ao ler o conteudo do arquivo enviado", ex);
        }

        Document document = Document.builder()
                .patientId(patient.getId())
                .appointmentId(appointmentId)
                .fileName(originalName)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .storageKey(storageKey)
                .description(description)
                .build();
        document = documentRepository.save(document);
        log.info("Documento registrado: id={}, paciente={}, key={}", document.getId(), patientId, storageKey);

        return documentMapper.toResponse(document, presign(storageKey));
    }

    @Override
    public DocumentResponse getById(UUID id) {
        Document document = getEntityOrThrow(id);
        return documentMapper.toResponse(document, presign(document.getStorageKey()));
    }

    @Override
    public PageResponse<DocumentResponse> listByPatient(UUID patientId, Pageable pageable) {
        return PageResponse.from(documentRepository.findByPatientId(patientId, pageable)
                .map(doc -> documentMapper.toResponse(doc, presign(doc.getStorageKey()))));
    }

    @Override
    public URL getDownloadUrl(UUID id) {
        Document document = getEntityOrThrow(id);
        return fileStorageService.generatePresignedDownloadUrl(document.getStorageKey());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Document document = getEntityOrThrow(id);
        fileStorageService.delete(document.getStorageKey());
        documentRepository.delete(document);
        log.info("Documento removido: id={}, key={}", id, document.getStorageKey());
    }

    private Document getEntityOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", id));
    }

    private String presign(String storageKey) {
        return fileStorageService.generatePresignedDownloadUrl(storageKey).toString();
    }

    private String buildKey(UUID patientId, String fileName) {
        return "patients/" + patientId + "/" + UUID.randomUUID() + "-" + fileName;
    }

    private String sanitize(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "arquivo";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
