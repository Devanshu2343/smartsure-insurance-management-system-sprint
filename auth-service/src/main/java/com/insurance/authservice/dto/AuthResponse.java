package com.insurance.authservice.dto;

import com.insurance.authservice.entity.Role;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private final String token;
    private final String tokenType;
    private final LocalDateTime expiresAt;
    private final Long userId;
    private final String email;
    private final Role role;
}
