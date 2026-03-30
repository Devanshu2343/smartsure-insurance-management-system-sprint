package com.insurance.authservice.service;

import com.insurance.authservice.dto.AuthResponse;
import com.insurance.authservice.dto.LoginRequest;
import com.insurance.authservice.dto.RegisterRequest;
import com.insurance.authservice.dto.UserResponse;

public interface AuthService {

    /**
     * Registers a new user account.
     *
     * @param registerRequest registration payload
     * @return registered user response
     */
    UserResponse register(RegisterRequest registerRequest);

    /**
     * Authenticates user and returns JWT token.
     *
     * @param loginRequest login payload
     * @return authentication response
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * Checks if a user exists by email.
     *
     * @param email email to validate
     * @return true if user exists
     */
    boolean isUserRegistered(String email);
}
