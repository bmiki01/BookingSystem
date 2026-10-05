package com.bookingsystem.common.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends AppException {
    public UserAlreadyExistsException(String email) {
        super("User with email '" + email + "' already exists", HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
    }
}
