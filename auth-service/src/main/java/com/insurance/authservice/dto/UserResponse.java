package com.insurance.authservice.dto;

import com.insurance.authservice.entity.Role;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private final Long id;
    private final String fullName;
    private final String email;
    private String phoneNumber;
    private String address;
    private final Role role;
    private final LocalDateTime createdAt;
}
