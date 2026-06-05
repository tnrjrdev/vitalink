package com.vitalink.platform.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s nao encontrado(a) com %s: %s", resource, field, value));
    }
}
