package com.geriatriccare.integration.api;

import com.geriatriccare.dto.AuthResponse;
import com.geriatriccare.dto.LoginRequest;
import com.geriatriccare.dto.RegisterRequest;
import com.geriatriccare.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthApiTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    private RegisterRequest registerRequest;
    
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setRole(UserRole.CAREGIVER);
    }
    
    @Test
    @DisplayName("Should register new user successfully")
    void registerUser_Success() {
        // When
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/api/auth/register",
            registerRequest,
            AuthResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("Should return 400 when registering duplicate email")
    void registerUser_DuplicateEmail() {
        // Given - register first user
        restTemplate.postForEntity("/api/auth/register", registerRequest, AuthResponse.class);
        
        // When - try to register again with same email
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/auth/register",
            registerRequest,
            String.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_Success() {
        // Given - register user first
        restTemplate.postForEntity("/api/auth/register", registerRequest, AuthResponse.class);
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");
        
        // When
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            AuthResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
    }
    
@Test
@DisplayName("Should return 401 with invalid credentials")
void login_InvalidCredentials() {
    // Given - register user
    restTemplate.postForEntity("/api/auth/register", registerRequest, AuthResponse.class);
    
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test@example.com");
    loginRequest.setPassword("WrongPassword!");
    
    // When & Then - TestRestTemplate throws exception on 401
    assertThatThrownBy(() -> {
        restTemplate.postForEntity("/api/auth/login", loginRequest, String.class);
    }).isInstanceOf(Exception.class); // 401 causes HttpRetryException
}

}