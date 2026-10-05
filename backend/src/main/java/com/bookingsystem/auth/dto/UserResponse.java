package com.bookingsystem.auth.dto;

import com.bookingsystem.auth.model.Role;
import com.bookingsystem.auth.model.User;

import java.time.Instant;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    Role role,
    Instant createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}
