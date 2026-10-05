package com.bookingsystem.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration request payload")
public record RegisterRequest(
    @Schema(description = "Valid email address", example = "user@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @Schema(description = "Password (minimum 8 characters)", example = "SecurePass123!")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password,

    @Schema(description = "User's full name", example = "John Doe")
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    String fullName
) {}
