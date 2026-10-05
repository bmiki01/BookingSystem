package com.bookingsystem.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT access token")
public record AuthResponse(
    @Schema(description = "JWT Access Token")
    String accessToken,

    @Schema(description = "Token type", example = "Bearer")
    String tokenType,

    @Schema(description = "User details")
    UserResponse user
) {
    public AuthResponse(String accessToken, UserResponse user) {
        this(accessToken, "Bearer", user);
    }
}
