package com.smartsure.auth.controller;


import com.smartsure.auth.dto.AuthResponse;
import com.smartsure.auth.dto.LoginRequest;
import com.smartsure.auth.dto.RegisterRequest;
import com.smartsure.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return "User registered successfully";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/test")
    public String test() {
        return "SECURED API WORKING ";
    }
}