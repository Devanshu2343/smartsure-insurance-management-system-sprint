package com.smartsure.auth.controller;

import com.smartsure.auth.dto.LoginRequest;
import com.smartsure.auth.dto.RegisterRequest;
import com.smartsure.auth.entity.User;
import com.smartsure.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    // To register User -> POST :: http://localhost:8081/api/auth/register
    /*
    {
  "name": "Devanshu",
  "email": "devanshu@gmail.com",
  "password": "123456",
  "role": "USER"
}
     */
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        return authService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        return authService.login(
                request.getEmail(),
                request.getPassword()
        );
    }
}