package com.bookingsystem.auth.service;

import com.bookingsystem.auth.dto.AuthResponse;
import com.bookingsystem.auth.dto.LoginRequest;
import com.bookingsystem.auth.dto.RegisterRequest;
import com.bookingsystem.auth.dto.UserResponse;
import com.bookingsystem.auth.model.Role;
import com.bookingsystem.auth.model.User;
import com.bookingsystem.auth.repository.UserRepository;
import com.bookingsystem.common.exception.ResourceNotFoundException;
import com.bookingsystem.common.exception.UserAlreadyExistsException;
import com.bookingsystem.security.JwtTokenProvider;
import com.bookingsystem.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtTokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return new AuthResponse(token, UserResponse.fromEntity(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.email()));

        return new AuthResponse(token, UserResponse.fromEntity(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        return UserResponse.fromEntity(user);
    }
}
