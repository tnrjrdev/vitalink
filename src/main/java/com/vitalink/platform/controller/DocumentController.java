package com.vitalink.platform.controller;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.document.DocumentResponse;
import com.vitalink.platform.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Documentos", description = "Upload e download de documentos clinicos (exames, laudos, prescricoes) via S3")
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC', 'PROFESSIONAL')")
    @Operation(summary = "Faz upload de um documento e o associa a um paciente")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam UUID patientId,
            @RequestParam(required = false) UUID appointmentId,
            @RequestParam(required = false) String description,
            UriComponentsBuilder uriBuilder) {
        DocumentResponse created = documentService.upload(file, patientId, appointmentId, description);
        URI location = uriBuilder.path("/api/v1/documents/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca metadados de um documento (inclui URL de download pre-assinada)")
    public ResponseEntity<DocumentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lista os documentos de um paciente")
    public ResponseEntity<PageResponse<DocumentResponse>> listByPatient(
            @RequestParam UUID patientId, Pageable pageable) {
        return ResponseEntity.ok(documentService.listByPatient(patientId, pageable));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Redireciona para a URL pre-assinada de download do arquivo")
    public ResponseEntity<Void> download(@PathVariable UUID id) {
        URI uri = URI.create(documentService.getDownloadUrl(id).toString());
        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC')")
    @Operation(summary = "Remove um documento (arquivo no storage + metadados)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
