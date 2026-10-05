package com.bookingsystem.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        log.warn("Application exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
            ex.getStatus().value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> new FieldErrorDto(err.getField(), err.getDefaultMessage()))
            .toList();

        log.warn("Validation failed for path {}: {} errors", request.getRequestURI(), fieldErrors.size());
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_FAILED",
            "Validation failed for input fields",
            fieldErrors,
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request at path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "MALFORMED_JSON",
            "Malformed JSON request payload or invalid parameter format",
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String msg = String.format("Parameter '%s' must be of type '%s'", ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "TYPE_MISMATCH",
            msg,
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Database constraint violation at path {}: {}", request.getRequestURI(), ex.getMessage());

        String message = "Database constraint violation occurred";
        String errorCode = "DATA_INTEGRITY_VIOLATION";

        if (ex.getMessage() != null && ex.getMessage().contains("no_overlapping_bookings")) {
            message = "The requested time slot was just booked by another user";
            errorCode = "BOOKING_OVERLAP_CONFLICT";
        }

        ErrorResponse response = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            errorCode,
            message,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(OptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Optimistic lock conflict at path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "OPTIMISTIC_LOCK_CONFLICT",
            "The resource was modified by another transaction. Please reload and try again.",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "INVALID_CREDENTIALS",
            "Invalid email or password",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "ACCESS_DENIED",
            "You do not have permission to access this resource",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at path {}: ", request.getRequestURI(), ex);
        ErrorResponse response = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            "An unexpected internal error occurred",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
