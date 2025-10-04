package com.geriatriccare.unit.service;

import com.geriatriccare.dto.AuthResponse;
import com.geriatriccare.dto.LoginRequest;
import com.geriatriccare.dto.RegisterRequest;
import com.geriatriccare.entity.User;
import com.geriatriccare.entity.UserRole;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.security.JwtUtil;
import com.geriatriccare.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.CAREGIVER);
        
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setRole(UserRole.CAREGIVER);
        
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }
    
    @Test
    @DisplayName("Should register new user successfully")
    void registerUser_Success() {
        // Given
        when(userRepository.existsByEmailAndIsActiveTrue(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateTokenFromUsername(anyString())).thenReturn("jwt-token");
        
        // When
        AuthResponse response = authService.registerUser(registerRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void registerUser_DuplicateEmail() {
        // Given
        when(userRepository.existsByEmailAndIsActiveTrue(anyString())).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Email address already in use");
        
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void authenticateUser_Success() {
        // Given
        when(authentication.getName()).thenReturn("test@example.com");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateTokenFromAuthentication(any(Authentication.class))).thenReturn("jwt-token");
        
        // When
        AuthResponse response = authService.authenticateUser(loginRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any());
    }
    
    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void authenticateUser_InvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Invalid credentials"));
        
        // When & Then
        assertThatThrownBy(() -> authService.authenticateUser(loginRequest))
            .isInstanceOf(BadCredentialsException.class);
        
        verify(jwtUtil, never()).generateTokenFromAuthentication(any());
    }
    
    @Test
    @DisplayName("Should validate token and return user")
    void validateToken_Success() {
        // Given
        String token = "valid-jwt-token";
        String email = "test@example.com";
        when(jwtUtil.validateJwtToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(email);
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(testUser));
        
        // When
        User result = authService.validateToken(token);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
    }
    
    @Test
    @DisplayName("Should return null for invalid token")
    void validateToken_Invalid() {
        String invalidToken = "invalid.token.value";
        assertThatThrownBy(() -> authService.validateToken(invalidToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid or expired token");
    }
    
    @Test
    @DisplayName("Should refresh token for existing user")
    void refreshToken_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateTokenFromUsername(email)).thenReturn("new-jwt-token");
        
        // When
        AuthResponse response = authService.refreshToken(email);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("new-jwt-token");
    }
    
    @Test
    @DisplayName("Should check if email exists")
    void existsByEmail() {
        // Given
        when(userRepository.existsByEmailAndIsActiveTrue("existing@example.com")).thenReturn(true);
        when(userRepository.existsByEmailAndIsActiveTrue("new@example.com")).thenReturn(false);
        
        // When & Then
        assertThat(authService.existsByEmail("existing@example.com")).isTrue();
        assertThat(authService.existsByEmail("new@example.com")).isFalse();
    }
}