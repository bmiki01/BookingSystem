package com.bookingsystem.auth.service;

import com.bookingsystem.auth.dto.AuthResponse;
import com.bookingsystem.auth.dto.LoginRequest;
import com.bookingsystem.auth.dto.RegisterRequest;
import com.bookingsystem.auth.model.Role;
import com.bookingsystem.auth.model.User;
import com.bookingsystem.auth.repository.UserRepository;
import com.bookingsystem.common.exception.UserAlreadyExistsException;
import com.bookingsystem.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "test@example.com", "encoded_password", "Test User", Role.USER, Instant.now(), Instant.now());
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("test@example.com", "Password123!", "Test User");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt_token_123");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt_token_123");
        assertThat(response.user().email()).isEqualTo("test@example.com");
        assertThat(response.user().fullName()).isEqualTo("Test User");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ThrowsUserAlreadyExistsException_WhenEmailExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "Password123!", "Test User");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt_token_123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(sampleUser));

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt_token_123");
        assertThat(response.user().email()).isEqualTo("test@example.com");
    }
}
