package com.smartsure.auth.service;

import com.smartsure.auth.entity.User;

public interface AuthService {
    String register(User user);

    String login(String email, String password);
}
