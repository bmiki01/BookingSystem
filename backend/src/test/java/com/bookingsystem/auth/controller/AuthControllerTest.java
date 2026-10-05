package com.bookingsystem.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bookingsystem.auth.dto.AuthResponse;
import com.bookingsystem.auth.dto.LoginRequest;
import com.bookingsystem.auth.dto.RegisterRequest;
import com.bookingsystem.auth.dto.UserResponse;
import com.bookingsystem.auth.model.Role;
import com.bookingsystem.auth.service.AuthService;
import com.bookingsystem.common.exception.UserAlreadyExistsException;
import com.bookingsystem.security.JwtTokenProvider;
import com.bookingsystem.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void register_Returns201Created_WhenInputIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("john@example.com", "Password123!", "John Doe");
        UserResponse userResponse = new UserResponse(1L, "john@example.com", "John Doe", Role.USER, Instant.now());
        AuthResponse authResponse = new AuthResponse("mock_token", userResponse);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("mock_token"))
            .andExpect(jsonPath("$.user.email").value("john@example.com"))
            .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void register_Returns400BadRequest_WhenValidationFails() throws Exception {
        RegisterRequest request = new RegisterRequest("invalid-email", "short", "");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void register_Returns409Conflict_WhenUserAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("john@example.com", "Password123!", "John Doe");

        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new UserAlreadyExistsException("john@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void login_Returns200Ok_WhenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "Password123!");
        UserResponse userResponse = new UserResponse(1L, "john@example.com", "John Doe", Role.USER, Instant.now());
        AuthResponse authResponse = new AuthResponse("mock_token", userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("mock_token"))
            .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    void getCurrentUser_Returns401Unauthorized_WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_Returns200Ok_WhenAuthenticated() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "john@example.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        UserResponse userResponse = new UserResponse(1L, "john@example.com", "John Doe", Role.USER, Instant.now());

        when(authService.getCurrentUser(any())).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me").with(authentication(auth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.fullName").value("John Doe"));
    }
}
