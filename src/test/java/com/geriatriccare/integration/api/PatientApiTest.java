package com.geriatriccare.integration.api;

import com.geriatriccare.dto.PatientRequest;
import com.geriatriccare.dto.PatientResponse;
import com.geriatriccare.dto.RegisterRequest;
import com.geriatriccare.dto.AuthResponse;
import com.geriatriccare.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PatientApiTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private String jwtToken;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        restTemplate = new TestRestTemplate();
        baseUrl = "http://localhost:" + port + "/api/patients";
        
        // Register and login to get JWT token
        jwtToken = getJwtToken();
    }

    private String getJwtToken() {
        // Register a user with OWNER role (has all permissions)
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("owner@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("Owner");
        registerRequest.setRole(UserRole.ADMIN);

        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/auth/register",
            registerRequest,
            AuthResponse.class
        );

        return registerResponse.getBody().getToken();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("Should create patient successfully with authentication")
    void createPatient_Success() {
        // ARRANGE
        PatientRequest request = new PatientRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1950, 1, 1));
        request.setMedicalConditions("Diabetes Type 2, Hypertension");
        request.setEmergencyContact("Jane Doe");
        request.setEmergencyPhone("555-0101");

        HttpEntity<PatientRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        // ACT
        ResponseEntity<PatientResponse> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            entity,
            PatientResponse.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("John");
        assertThat(response.getBody().getLastName()).isEqualTo("Doe");
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getAge()).isGreaterThan(0);
        assertThat(response.getBody().getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should retrieve patient by ID")
    void getPatient_Success() {
        // ARRANGE - Create a patient first
        PatientRequest createRequest = new PatientRequest();
        createRequest.setFirstName("Jane");
        createRequest.setLastName("Smith");
        createRequest.setDateOfBirth(LocalDate.of(1960, 5, 15));
        createRequest.setMedicalConditions("Arthritis");
        createRequest.setEmergencyContact("John Smith");
        createRequest.setEmergencyPhone("555-0201");

        HttpEntity<PatientRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        ResponseEntity<PatientResponse> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            createEntity,
            PatientResponse.class
        );
        
        String patientId = createResponse.getBody().getId().toString();

        // ACT - Retrieve the patient
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<PatientResponse> response = restTemplate.exchange(
            baseUrl + "/" + patientId,
            HttpMethod.GET,
            getEntity,
            PatientResponse.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("Jane");
        assertThat(response.getBody().getLastName()).isEqualTo("Smith");
        assertThat(response.getBody().getMedicalConditions()).isEqualTo("Arthritis");
    }

    @Test
    @DisplayName("Should update patient successfully")
    void updatePatient_Success() {
        // ARRANGE - Create a patient first
        PatientRequest createRequest = new PatientRequest();
        createRequest.setFirstName("Original");
        createRequest.setLastName("Name");
        createRequest.setDateOfBirth(LocalDate.of(1955, 3, 10));
        createRequest.setMedicalConditions("None");
        createRequest.setEmergencyContact("Contact Person");
        createRequest.setEmergencyPhone("555-0301");

        HttpEntity<PatientRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        ResponseEntity<PatientResponse> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            createEntity,
            PatientResponse.class
        );
        
        String patientId = createResponse.getBody().getId().toString();

        // Update request
        PatientRequest updateRequest = new PatientRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setDateOfBirth(LocalDate.of(1955, 3, 10));
        updateRequest.setMedicalConditions("Diabetes Type 2");
        updateRequest.setEmergencyContact("New Contact");
        updateRequest.setEmergencyPhone("555-0999");

        HttpEntity<PatientRequest> updateEntity = new HttpEntity<>(updateRequest, createAuthHeaders());

        // ACT
        ResponseEntity<PatientResponse> response = restTemplate.exchange(
            baseUrl + "/" + patientId,
            HttpMethod.PUT,
            updateEntity,
            PatientResponse.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("Updated");
        assertThat(response.getBody().getMedicalConditions()).isEqualTo("Diabetes Type 2");
        assertThat(response.getBody().getEmergencyPhone()).isEqualTo("555-0999");
    }

    @Test
    @DisplayName("Should soft delete patient successfully")
    void deletePatient_Success() {
        // ARRANGE - Create a patient first
        PatientRequest createRequest = new PatientRequest();
        createRequest.setFirstName("ToDelete");
        createRequest.setLastName("Patient");
        createRequest.setDateOfBirth(LocalDate.of(1945, 8, 20));
        createRequest.setEmergencyContact("Emergency Contact");
        createRequest.setEmergencyPhone("555-0401");

        HttpEntity<PatientRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        ResponseEntity<PatientResponse> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            createEntity,
            PatientResponse.class
        );
        
        String patientId = createResponse.getBody().getId().toString();

        HttpEntity<Void> deleteEntity = new HttpEntity<>(createAuthHeaders());

        // ACT
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + patientId,
            HttpMethod.DELETE,
            deleteEntity,
            Void.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify patient is soft deleted (trying to get it should fail)
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> getResponse = restTemplate.exchange(
            baseUrl + "/" + patientId,
            HttpMethod.GET,
            getEntity,
            String.class
        );
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 400 when creating patient with invalid data")
    void createPatient_ValidationError() {
        // ARRANGE - Missing required field (firstName)
        PatientRequest request = new PatientRequest();
        // firstName is missing - validation should fail
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1950, 1, 1));

        HttpEntity<PatientRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            entity,
            String.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 400 when date of birth is in future")
    void createPatient_FutureDateOfBirth() {
        // ARRANGE
        PatientRequest request = new PatientRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.now().plusYears(1)); // Future date
        request.setEmergencyContact("Jane Doe");
        request.setEmergencyPhone("555-0101");

        HttpEntity<PatientRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        // ACT
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            entity,
            String.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}