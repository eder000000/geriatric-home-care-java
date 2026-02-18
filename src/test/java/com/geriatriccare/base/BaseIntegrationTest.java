package com.geriatriccare.base;

import com.geriatriccare.dto.AuthResponse;
import com.geriatriccare.dto.LoginRequest;
import com.geriatriccare.dto.RegisterRequest;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.repository.UserRepository;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    protected RestTemplate restTemplate;

    protected String baseUrl;
    protected String adminToken;
    protected String physicianToken;
    protected String caregiverToken;
    protected String familyToken;
    protected String ownerToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        CloseableHttpClient httpClient = HttpClients.custom()
                .disableRedirectHandling()
                .build();
        restTemplate = new RestTemplate(
            new HttpComponentsClientHttpRequestFactory(httpClient));
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(HttpStatusCode statusCode) {
                return false;
            }
        });
        cleanDatabase();
        setupTestUsers();
        ownerToken = adminToken;
    }

    protected void cleanDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("DELETE FROM care_tasks");
            stmt.execute("DELETE FROM care_plans");
            stmt.execute("DELETE FROM care_plan_templates");
            stmt.execute("DELETE FROM vital_signs");
            stmt.execute("DELETE FROM alerts");
            stmt.execute("DELETE FROM alert_rules");
            stmt.execute("DELETE FROM medications");
            stmt.execute("DELETE FROM patient_caregivers");
            stmt.execute("DELETE FROM patient_family_members");
            stmt.execute("DELETE FROM patients");
            stmt.execute("DELETE FROM adherence_reports");
            stmt.execute("DELETE FROM medication_adherence_reports");
            stmt.execute("DELETE FROM password_history");
            stmt.execute("DELETE FROM users");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        } catch (Exception e) {
            System.err.println("[BaseIntegrationTest] cleanDatabase warning: " + e.getMessage());
        }
    }

    protected void setupTestUsers() {
        adminToken     = registerAndLogin("admin@test.com",     "TestPass123!", "Test", "Admin",     UserRole.ADMIN);
        physicianToken = registerAndLogin("physician@test.com", "TestPass123!", "Test", "Physician", UserRole.PHYSICIAN);
        caregiverToken = registerAndLogin("caregiver@test.com", "TestPass123!", "Test", "Caregiver", UserRole.CAREGIVER);
        familyToken    = registerAndLogin("family@test.com",    "TestPass123!", "Test", "Family",    UserRole.FAMILY);
    }

    protected String registerAndLogin(String email, String password,
                                      String firstName, String lastName, UserRole role) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword(password);
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setRole(role);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            baseUrl + "/api/auth/register", req, AuthResponse.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null
                && response.getBody().getToken() != null) {
            return response.getBody().getToken();
        }
        return loginAndGetToken(email, password);
    }

    protected String loginAndGetToken(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            baseUrl + "/api/auth/login", req, AuthResponse.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getToken();
        }
        throw new RuntimeException("Login failed for " + email + " — " + response.getStatusCode());
    }

    protected HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected <T> ResponseEntity<T> getWithAuth(String url, String token, Class<T> responseType) {
        return restTemplate.exchange(baseUrl + url, HttpMethod.GET,
            new HttpEntity<>(createAuthHeaders(token)), responseType);
    }

    protected <T> ResponseEntity<T> postWithAuth(String url, Object body, String token, Class<T> responseType) {
        return restTemplate.exchange(baseUrl + url, HttpMethod.POST,
            new HttpEntity<>(body, createAuthHeaders(token)), responseType);
    }

    protected <T> ResponseEntity<T> putWithAuth(String url, Object body, String token, Class<T> responseType) {
        return restTemplate.exchange(baseUrl + url, HttpMethod.PUT,
            new HttpEntity<>(body, createAuthHeaders(token)), responseType);
    }

    protected <T> ResponseEntity<T> deleteWithAuth(String url, String token, Class<T> responseType) {
        return restTemplate.exchange(baseUrl + url, HttpMethod.DELETE,
            new HttpEntity<>(createAuthHeaders(token)), responseType);
    }

    protected ResponseEntity<String> postNoAuth(String url, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(baseUrl + url, HttpMethod.POST,
            new HttpEntity<>(body, headers), String.class);
    }
}
