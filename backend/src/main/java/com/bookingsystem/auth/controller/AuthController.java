package com.bookingsystem.auth.controller;

import com.bookingsystem.auth.dto.AuthResponse;
import com.bookingsystem.auth.dto.LoginRequest;
import com.bookingsystem.auth.dto.RegisterRequest;
import com.bookingsystem.auth.dto.UserResponse;
import com.bookingsystem.auth.service.AuthService;
import com.bookingsystem.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration, login, and profile endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account", description = "Creates a new user with ROLE_USER and returns a JWT access token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token", description = "Authenticates user credentials and returns a valid JWT access token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", description = "Returns details of the currently authenticated user based on JWT token")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse response = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(response);
    }
}
