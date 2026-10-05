package com.bookingsystem.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException() {
        super("Invalid email or password", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
}
