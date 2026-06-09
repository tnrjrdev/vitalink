package com.vitalink.platform.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Recurso nao encontrado: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex, WebRequest request) {
        log.warn("Conflito de unicidade: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, WebRequest request) {
        log.warn("Regra de negocio violada: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        log.warn("Falha de autenticacao: credenciais invalidas");
        return build(HttpStatus.UNAUTHORIZED, "Credenciais invalidas", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "Acesso negado: voce nao possui permissao para este recurso", request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(ObjectOptimisticLockingFailureException ex, WebRequest request) {
        log.warn("Conflito de concorrencia (optimistic lock): {}", ex.getMessage());
        return build(HttpStatus.CONFLICT,
                "O registro foi modificado por outra operacao. Recarregue os dados e tente novamente.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Violacao de integridade de dados: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT,
                "Operacao viola uma restricao de integridade dos dados.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<ApiError.FieldValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());
        log.warn("Falha de validacao: {} campo(s) invalido(s)", fieldErrors.size());

        ApiError body = baseBuilder(HttpStatus.BAD_REQUEST, "Erro de validacao dos dados de entrada", request)
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<ApiError.FieldValidationError> fieldErrors = ex.getConstraintViolations().stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());

        ApiError body = baseBuilder(HttpStatus.BAD_REQUEST, "Erro de validacao dos parametros", request)
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex, WebRequest request) {
        log.warn("Upload rejeitado: arquivo excede o tamanho maximo permitido");
        return build(HttpStatus.PAYLOAD_TOO_LARGE,
                "O arquivo enviado excede o tamanho maximo permitido.", request);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiError> handleStorage(StorageException ex, WebRequest request) {
        log.error("Falha no armazenamento de arquivos: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY,
                "Falha ao processar o arquivo no servico de armazenamento. Tente novamente mais tarde.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        log.error("Erro interno nao tratado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado. Tente novamente mais tarde.", request);
    }

    private ApiError.FieldValidationError toFieldError(FieldError fieldError) {
        return ApiError.FieldValidationError.builder()
                .field(fieldError.getField())
                .rejectedValue(fieldError.getRejectedValue())
                .message(fieldError.getDefaultMessage())
                .build();
    }

    private ApiError.FieldValidationError toFieldError(ConstraintViolation<?> violation) {
        return ApiError.FieldValidationError.builder()
                .field(violation.getPropertyPath().toString())
                .rejectedValue(violation.getInvalidValue())
                .message(violation.getMessage())
                .build();
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, WebRequest request) {
        return ResponseEntity.status(status).body(baseBuilder(status, message, request).build());
    }

    private ApiError.ApiErrorBuilder baseBuilder(HttpStatus status, String message, WebRequest request) {
        return ApiError.builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(extractPath(request));
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replaceFirst("^uri=", "");
    }
}
