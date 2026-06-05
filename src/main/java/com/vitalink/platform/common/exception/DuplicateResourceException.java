package com.vitalink.platform.common.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s ja cadastrado(a) com %s: %s", resource, field, value));
    }
}
