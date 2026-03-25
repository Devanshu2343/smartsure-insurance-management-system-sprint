package com.smartsure.auth.service;

import com.smartsure.auth.dto.AuthResponse;
import com.smartsure.auth.dto.LoginRequest;
import com.smartsure.auth.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
