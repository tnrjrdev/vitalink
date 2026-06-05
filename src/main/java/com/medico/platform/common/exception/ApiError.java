package com.medico.platform.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * corpo de resposta padronizado para todos os erros da API.
 * inspirado no RFC 7807 (Problem Details), mantendo um formato estavel e
 * previsivel para os consumidores.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    /** Momento em que o erro ocorreu (UTC). */
    private final OffsetDateTime timestamp;

    /** Codigo HTTP. */
    private final int status;

    /** Frase associada ao status HTTP (ex.: "Not Found"). */
    private final String error;

    /** Mensagem legivel descrevendo o problema. */
    private final String message;

    /** Caminho (URI) que originou o erro. */
    private final String path;

    /** Detalhes campo-a-campo em erros de validacao (opcional). */
    private final List<FieldValidationError> fieldErrors;

    @Getter
    @Builder
    public static class FieldValidationError {
        private final String field;
        private final Object rejectedValue;
        private final String message;
    }
}
