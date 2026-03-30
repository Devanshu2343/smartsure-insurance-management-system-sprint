package com.insurance.authservice.service.impl;

import com.insurance.authservice.config.JwtUtil;
import com.insurance.authservice.dto.AuthResponse;
import com.insurance.authservice.dto.LoginRequest;
import com.insurance.authservice.dto.RegisterRequest;
import com.insurance.authservice.dto.UserResponse;
import com.insurance.authservice.entity.Role;
import com.insurance.authservice.entity.User;
import com.insurance.authservice.exception.InvalidCredentialsException;
import com.insurance.authservice.exception.ResourceAlreadyExistsException;
import com.insurance.authservice.repository.UserRepository;
import com.insurance.authservice.service.AuthService;
import com.insurance.authservice.service.CustomUserDetailsService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public UserResponse register(RegisterRequest registerRequest) {
        String normalizedEmail = normalizeEmail(registerRequest.getEmail());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResourceAlreadyExistsException("Email is already registered: " + normalizedEmail);
        }

        User user = new User();

        user.setFullName(registerRequest.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(
                registerRequest.getRole() == null
                        ? Role.CUSTOMER : registerRequest.getRole());

        user.setAddress(
                registerRequest.getAddress() != null
                        ? registerRequest.getAddress().trim()
                        : null
        );

        user.setPhoneNumber(
                registerRequest.getPhoneNumber() != null
                        ? registerRequest.getPhoneNumber().trim()
                        : null
        );

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with email: {} and role: {}", savedUser.getEmail(), savedUser.getRole());

        return UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .address(savedUser.getAddress())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        String normalizedEmail = normalizeEmail(loginRequest.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(customUserDetailsService.loadUserByUsername(user.getEmail()));
        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(jwtUtil.extractExpiry(token))
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public boolean isUserRegistered(String email) {
        String normalizedEmail = normalizeEmail(email);
        boolean exists = userRepository.existsByEmailIgnoreCase(normalizedEmail);
        log.info("User validation checked. email={}, exists={}", normalizedEmail, exists);
        return exists;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
