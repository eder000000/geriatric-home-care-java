package com.geriatriccare.base;

import com.geriatriccare.dto.LoginRequest;
import com.geriatriccare.dto.AuthResponse;
import com.geriatriccare.dto.RegisterRequest;
import com.geriatriccare.entity.User;
import com.geriatriccare.entity.UserRole;
import com.geriatriccare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    
    @LocalServerPort
    protected int port;
    
    @Autowired
    protected TestRestTemplate restTemplate;
    
    @Autowired
    protected UserRepository userRepository;
    
    protected String baseUrl;
    protected String ownerToken;
    protected String caregiverToken;
    protected User ownerUser;
    protected User caregiverUser;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        cleanDatabase();
        setupTestUsers();
    }
    
    protected void cleanDatabase() {
        userRepository.deleteAll();
    }
    
    protected void setupTestUsers() {
        // Create owner user
        RegisterRequest ownerReq = new RegisterRequest();
        ownerReq.setEmail("owner@test.com");
        ownerReq.setPassword("TestPass123!");
        ownerReq.setFirstName("Test");
        ownerReq.setLastName("Owner");
        ownerReq.setRole(UserRole.OWNER);
        
        restTemplate.postForEntity(
            baseUrl + "/api/auth/register",
            ownerReq,
            Void.class
        );
        
        ownerToken = loginAndGetToken("owner@test.com", "TestPass123!");
        ownerUser = userRepository.findByEmail("owner@test.com").orElseThrow();
        
        // Create caregiver user
        RegisterRequest caregiverReq = new RegisterRequest();
        caregiverReq.setEmail("caregiver@test.com");
        caregiverReq.setPassword("TestPass123!");
        caregiverReq.setFirstName("Test");
        caregiverReq.setLastName("Caregiver");
        caregiverReq.setRole(UserRole.CAREGIVER);
        
        restTemplate.postForEntity(
            baseUrl + "/api/auth/register",
            caregiverReq,
            Void.class
        );
        
        caregiverToken = loginAndGetToken("caregiver@test.com", "TestPass123!");
        caregiverUser = userRepository.findByEmail("caregiver@test.com").orElseThrow();
    }
    
    protected String loginAndGetToken(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            baseUrl + "/api/auth/login",
            loginRequest,
            AuthResponse.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getToken();
        }
        throw new RuntimeException("Failed to login user: " + email);
    }
    
    protected HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    // ADD THESE MISSING METHODS
    protected <T> ResponseEntity<T> getWithAuth(String url, String token, Class<T> responseType) {
        return restTemplate.exchange(
            baseUrl + url,
            HttpMethod.GET,
            new HttpEntity<>(createAuthHeaders(token)),
            responseType
        );
    }
    
    protected <T> ResponseEntity<T> postWithAuth(String url, Object request, String token, Class<T> responseType) {
        return restTemplate.exchange(
            baseUrl + url,
            HttpMethod.POST,
            new HttpEntity<>(request, createAuthHeaders(token)),
            responseType
        );
    }
    
    protected <T> ResponseEntity<T> putWithAuth(String url, Object request, String token, Class<T> responseType) {
        return restTemplate.exchange(
            baseUrl + url,
            HttpMethod.PUT,
            new HttpEntity<>(request, createAuthHeaders(token)),
            responseType
        );
    }
    
    protected <T> ResponseEntity<T> deleteWithAuth(String url, String token, Class<T> responseType) {
        return restTemplate.exchange(
            baseUrl + url,
            HttpMethod.DELETE,
            new HttpEntity<>(createAuthHeaders(token)),
            responseType
        );
    }
}