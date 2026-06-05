package com.vitalink.platform.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private final OffsetDateTime timestamp;

    private final int status;

    private final String error;

    private final String message;

    private final String path;

    private final List<FieldValidationError> fieldErrors;

    @Getter
    @Builder
    public static class FieldValidationError {
        private final String field;
        private final Object rejectedValue;
        private final String message;
    }
}
