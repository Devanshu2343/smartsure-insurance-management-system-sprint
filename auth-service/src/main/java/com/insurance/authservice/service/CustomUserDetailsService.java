package com.insurance.authservice.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {

    @Override
    UserDetails loadUserByUsername(String username);
}
