package com.smartsure.auth.service.impl;

import com.smartsure.auth.config.JwtUtil;
import com.smartsure.auth.entity.Role;
import com.smartsure.auth.entity.User;
import com.smartsure.auth.repository.UserRepository;
import com.smartsure.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public String register(User user) {


        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }


        user.setPassword(passwordEncoder.encode(user.getPassword()));


        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        userRepository.save(user);

        return "User registered successfully";
    }


    @Override
    public String login(String email, String password) {


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials")); // 🔥 security best practice


        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }


        return jwtUtil.generateToken(user);
    }
}