package com.insurance.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.insurance.authservice.entity.Role;
import com.insurance.authservice.entity.User;
import com.insurance.authservice.exception.ResourceNotFoundException;
import com.insurance.authservice.repository.UserRepository;
import com.insurance.authservice.service.impl.CustomUserDetailsServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @Test
    void loadUserByUsernameReturnsUserDetailsWhenFound() {
        // Arrange
        User user = new User();
        user.setEmail("admin@test.com");
        user.setPassword("encoded");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmailIgnoreCase(eq("admin@test.com")))
                .thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@test.com");

        // Assert
        assertThat(userDetails.getUsername()).isEqualTo("admin@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("encoded");
        assertThat(userDetails.getAuthorities())
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    void loadUserByUsernameThrowsWhenUserMissing() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase(eq("missing@test.com")))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@test.com"));
    }
}
