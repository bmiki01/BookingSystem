package com.bookingsystem.common.exception;

public record FieldErrorDto(
    String field,
    String message
) {}
