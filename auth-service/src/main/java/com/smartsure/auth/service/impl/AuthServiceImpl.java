package com.smartsure.auth.service.impl;


import com.smartsure.auth.dto.AuthResponse;
import com.smartsure.auth.dto.LoginRequest;
import com.smartsure.auth.dto.RegisterRequest;
import com.smartsure.auth.entity.Role;
import com.smartsure.auth.entity.User;
import com.smartsure.auth.repository.UserRepository;
import com.smartsure.auth.service.AuthService;
import com.smartsure.auth.util.JwtUtil;
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
    public void register(RegisterRequest request) {

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }



    @Override
    public AuthResponse login(LoginRequest request) {


        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }


        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );


        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .build();
    }
}
