package com.medico.platform.common.exception;

/**
 * lancada quando um recurso solicitado nao existe. Resulta em HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * construtor utilitario padronizado: "Paciente nao encontrado com id: {valor}".
     *
     * @param resource nome do recurso (ex.: "Paciente")
     * @param field    campo de busca (ex.: "id")
     * @param value    valor pesquisado
     */
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s nao encontrado(a) com %s: %s", resource, field, value));
    }
}
