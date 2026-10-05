package com.bookingsystem.common.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String errorCode,
    String message,
    List<FieldErrorDto> fieldErrors,
    String path
) {
    public ErrorResponse(int status, String errorCode, String message, String path) {
        this(Instant.now(), status, errorCode, message, List.of(), path);
    }

    public ErrorResponse(int status, String errorCode, String message, List<FieldErrorDto> fieldErrors, String path) {
        this(Instant.now(), status, errorCode, message, fieldErrors, path);
    }
}
