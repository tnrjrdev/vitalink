package com.medico.platform.common.exception;

/**
 * lancada quando se tenta criar um recurso que viola uma restricao de unicidade
 * (ex.: CPF, CNPJ ou e-mail ja cadastrados). Resulta em HTTP 409 (Conflict).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s ja cadastrado(a) com %s: %s", resource, field, value));
    }
}
