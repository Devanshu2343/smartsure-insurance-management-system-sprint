package com.insurance.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.insurance.authservice.config.JwtUtil;
import com.insurance.authservice.dto.AuthResponse;
import com.insurance.authservice.dto.LoginRequest;
import com.insurance.authservice.dto.RegisterRequest;
import com.insurance.authservice.dto.UserResponse;
import com.insurance.authservice.entity.Role;
import com.insurance.authservice.entity.User;
import com.insurance.authservice.exception.InvalidCredentialsException;
import com.insurance.authservice.exception.ResourceAlreadyExistsException;
import com.insurance.authservice.repository.UserRepository;
import com.insurance.authservice.service.impl.AuthServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerCreatesCustomerWhenRoleIsMissing() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setFullName("  John Doe ");
        request.setEmail("John.Doe@Example.com ");
        request.setPassword("Password123");
        request.setPhoneNumber("1234567890");
        request.setAddress("  Main Street ");

        when(userRepository.existsByEmailIgnoreCase(eq("john.doe@example.com"))).thenReturn(false);
        when(passwordEncoder.encode(eq("Password123"))).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertEquals(99L, response.getId());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals(Role.CUSTOMER, response.getRole());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertEquals("John Doe", savedUser.getFullName());
        assertEquals("john.doe@example.com", savedUser.getEmail());
        assertEquals("encoded-pass", savedUser.getPassword());
        assertEquals("1234567890", savedUser.getPhoneNumber());
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setFullName("User");
        request.setEmail("user@test.com");
        request.setPassword("Password123");
        request.setPhoneNumber("1234567890");

        when(userRepository.existsByEmailIgnoreCase(eq("user@test.com"))).thenReturn(true);

        // Act + Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreValid() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setPassword("encoded");
        user.setRole(Role.CUSTOMER);

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user@test.com")
                .password("encoded")
                .roles("CUSTOMER")
                .build();

        when(userRepository.findByEmailIgnoreCase(eq("user@test.com")))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("password"), eq("encoded"))).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername(eq("user@test.com")))
                .thenReturn(userDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(jwtUtil.extractExpiry(eq("jwt-token"))).thenReturn(LocalDateTime.now());

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("user@test.com");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        // Arrange
        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword("encoded");

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrong");

        when(userRepository.findByEmailIgnoreCase(eq("user@test.com")))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("wrong"), eq("encoded"))).thenReturn(false);

        // Act + Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginThrowsWhenUserIsMissing() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@test.com");
        request.setPassword("password");

        when(userRepository.findByEmailIgnoreCase(eq("missing@test.com")))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void isUserRegisteredNormalizesEmail() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(eq("user@test.com"))).thenReturn(true);

        // Act
        boolean exists = authService.isUserRegistered(" User@Test.com ");

        // Assert
        assertThat(exists).isTrue();
        verify(userRepository).existsByEmailIgnoreCase(eq("user@test.com"));
    }
}
