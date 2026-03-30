package com.insurance.authservice.service.impl;

import com.insurance.authservice.entity.User;
import com.insurance.authservice.exception.ResourceNotFoundException;
import com.insurance.authservice.repository.UserRepository;
import com.insurance.authservice.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found for email: " + username));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
