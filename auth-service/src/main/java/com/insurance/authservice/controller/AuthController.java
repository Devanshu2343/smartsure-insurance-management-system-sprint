package com.insurance.authservice.controller;

import com.insurance.authservice.dto.AuthResponse;
import com.insurance.authservice.dto.LoginRequest;
import com.insurance.authservice.dto.RegisterRequest;
import com.insurance.authservice.dto.UserResponse;
import com.insurance.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints for registration, login, and user validation.
 */
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping("/validate")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Boolean> validateUser(@RequestParam("email") @NotBlank @Email String email) {
        return ResponseEntity.ok(authService.isUserRegistered(email));
    }
}
