package com.geriatriccare.integration.api;

import com.geriatriccare.dto.*;
import com.geriatriccare.entity.CarePlanPriority;
import com.geriatriccare.entity.CarePlanStatus;
import com.geriatriccare.entity.UserRole;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CarePlanApiTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private String ownerToken;
    private String caregiverToken;
    private String baseUrl;
    private UUID testPatientId;
    private UUID testCaregiverId;

    @BeforeEach
    void setUp() {
        restTemplate = new TestRestTemplate();
        baseUrl = "http://localhost:" + port + "/api/care-plans";
        
        // Setup: Create owner user and get token
        ownerToken = registerUser("owner@test.com", "Owner", "User", UserRole.OWNER);
        
        // Setup: Create caregiver user and get token
        caregiverToken = registerUser("caregiver@test.com", "Caregiver", "User", UserRole.CAREGIVER);
        
        // Setup: Create a test patient (using owner token)
        testPatientId = createTestPatient();
        
        // Setup: Get caregiver ID for assignment
        testCaregiverId = getCaregiverUserId();
    }

    private String registerUser(String email, String firstName, String lastName, UserRole role) {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName(firstName);
        registerRequest.setLastName(lastName);
        registerRequest.setRole(role);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/auth/register",
            registerRequest,
            AuthResponse.class
        );

        return response.getBody().getToken();
    }

    private UUID createTestPatient() {
        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setFirstName("Test");
        patientRequest.setLastName("Patient");
        patientRequest.setDateOfBirth(LocalDate.of(1950, 1, 1));
        patientRequest.setEmergencyContact("Emergency Contact");
        patientRequest.setEmergencyPhone("555-0000");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + ownerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PatientRequest> entity = new HttpEntity<>(patientRequest, headers);

        ResponseEntity<PatientResponse> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/patients",
            HttpMethod.POST,
            entity,
            PatientResponse.class
        );

        return response.getBody().getId();
    }

    private UUID getCaregiverUserId() {
        // The caregiver user was created in setUp, we can extract ID from token
        // For simplicity, register and get the user back from auth response
        // In real scenario, you'd parse JWT or call a user endpoint
        // For now, we'll create another caregiver if needed
        RegisterRequest caregiverRequest = new RegisterRequest();
        caregiverRequest.setEmail("assigned-caregiver@test.com");
        caregiverRequest.setPassword("Password123!");
        caregiverRequest.setFirstName("Assigned");
        caregiverRequest.setLastName("Caregiver");
        caregiverRequest.setRole(UserRole.CAREGIVER);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/auth/register",
            caregiverRequest,
            AuthResponse.class
        );

        return response.getBody().getUserId();
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("Should create care plan successfully with patient and caregiver")
    void createCarePlan_Success() {
        // ARRANGE
        CarePlanRequest request = new CarePlanRequest();
        request.setPatientId(testPatientId);
        request.setTitle("Diabetes Management Plan");
        request.setDescription("Comprehensive diabetes care plan");
        request.setPriority(CarePlanPriority.HIGH);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusMonths(3));
        request.setAssignedCaregiverId(testCaregiverId);

        HttpEntity<CarePlanRequest> entity = new HttpEntity<>(request, createAuthHeaders(ownerToken));

        // ACT
        ResponseEntity<CarePlanResponse> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            entity,
            CarePlanResponse.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Diabetes Management Plan");
        assertThat(response.getBody().getStatus()).isEqualTo(CarePlanStatus.DRAFT);
        assertThat(response.getBody().getPriority()).isEqualTo(CarePlanPriority.HIGH);
        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    @DisplayName("Should retrieve care plan by ID")
    void getCarePlan_Success() {
        // ARRANGE - Create a care plan first
        CarePlanRequest createRequest = new CarePlanRequest();
        createRequest.setPatientId(testPatientId);
        createRequest.setTitle("Hypertension Management");
        createRequest.setDescription("Blood pressure monitoring plan");
        createRequest.setPriority(CarePlanPriority.MEDIUM);
        createRequest.setStartDate(LocalDate.now());
        createRequest.setEndDate(LocalDate.now().plusMonths(6));
        createRequest.setAssignedCaregiverId(testCaregiverId);

        HttpEntity<CarePlanRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            createEntity,
            CarePlanResponse.class
        );
        
        UUID carePlanId = createResponse.getBody().getId();

        // ACT - Retrieve the care plan
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> response = restTemplate.exchange(
            baseUrl + "/" + carePlanId,
            HttpMethod.GET,
            getEntity,
            CarePlanResponse.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Hypertension Management");
        assertThat(response.getBody().getPriority()).isEqualTo(CarePlanPriority.MEDIUM);
    }

    @Test
    @DisplayName("Should update care plan successfully")
    void updateCarePlan_Success() {
        // ARRANGE - Create a care plan first IN THIS TEST
        CarePlanRequest createRequest = new CarePlanRequest();
        createRequest.setPatientId(testPatientId);
        createRequest.setTitle("Original Plan");
        createRequest.setDescription("Original description");
        createRequest.setPriority(CarePlanPriority.LOW);
        createRequest.setStartDate(LocalDate.now());
        createRequest.setEndDate(LocalDate.now().plusMonths(1));
        createRequest.setAssignedCaregiverId(testCaregiverId);

        HttpEntity<CarePlanRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            createEntity,
            CarePlanResponse.class
        );
        
        UUID carePlanId = createResponse.getBody().getId();
        
        // Verify it was created successfully
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Update request
        CarePlanUpdateRequest updateRequest = new CarePlanUpdateRequest();
        updateRequest.setTitle("Updated Plan");
        updateRequest.setDescription("Updated description");
        updateRequest.setPriority(CarePlanPriority.URGENT);
        updateRequest.setStartDate(LocalDate.now());
        updateRequest.setEndDate(LocalDate.now().plusMonths(2));

        HttpEntity<CarePlanUpdateRequest> updateEntity = new HttpEntity<>(updateRequest, createAuthHeaders(ownerToken));

        // ACT
        ResponseEntity<CarePlanResponse> response = restTemplate.exchange(
            baseUrl + "/" + carePlanId,
            HttpMethod.PUT,
            updateEntity,
            CarePlanResponse.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Plan");
        assertThat(response.getBody().getDescription()).isEqualTo("Updated description");
        assertThat(response.getBody().getPriority()).isEqualTo(CarePlanPriority.URGENT);
    }

    @Test
    @DisplayName("Should activate care plan successfully")
    void activateCarePlan_Success() {
        // ARRANGE - Create a care plan in DRAFT status
        CarePlanRequest createRequest = new CarePlanRequest();
        createRequest.setPatientId(testPatientId);
        createRequest.setTitle("Plan to Activate");
        createRequest.setPriority(CarePlanPriority.HIGH);
        createRequest.setStartDate(LocalDate.now());
        createRequest.setEndDate(LocalDate.now().plusMonths(2));
        createRequest.setAssignedCaregiverId(testCaregiverId);

        HttpEntity<CarePlanRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            createEntity,
            CarePlanResponse.class
        );
        
        UUID carePlanId = createResponse.getBody().getId();
        assertThat(createResponse.getBody().getStatus()).isEqualTo(CarePlanStatus.DRAFT);

        HttpEntity<Void> activateEntity = new HttpEntity<>(createAuthHeaders(ownerToken));

        // ACT - Activate the care plan
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + carePlanId + "/activate",
            HttpMethod.PUT,
            activateEntity,
            Void.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify status changed to ACTIVE
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> getResponse = restTemplate.exchange(
            baseUrl + "/" + carePlanId,
            HttpMethod.GET,
            getEntity,
            CarePlanResponse.class
        );

        assertThat(getResponse.getBody().getStatus()).isEqualTo(CarePlanStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should soft delete care plan successfully")
    void deleteCarePlan_Success() {
        // ARRANGE - Create a care plan first
        CarePlanRequest createRequest = new CarePlanRequest();
        createRequest.setPatientId(testPatientId);
        createRequest.setTitle("Plan to Delete");
        createRequest.setPriority(CarePlanPriority.LOW);
        createRequest.setStartDate(LocalDate.now());
        createRequest.setEndDate(LocalDate.now().plusMonths(1));

        HttpEntity<CarePlanRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            createEntity,
            CarePlanResponse.class
        );
        
        UUID carePlanId = createResponse.getBody().getId();

        HttpEntity<Void> deleteEntity = new HttpEntity<>(createAuthHeaders(ownerToken));

        // ACT
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + carePlanId,
            HttpMethod.DELETE,
            deleteEntity,
            Void.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify care plan is soft deleted (trying to get it should fail)
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders(ownerToken));
        ResponseEntity<String> getResponse = restTemplate.exchange(
            baseUrl + "/" + carePlanId,
            HttpMethod.GET,
            getEntity,
            String.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 400 when creating care plan with missing required fields")
    void createCarePlan_ValidationError() {
        // ARRANGE - Missing title (required field)
        CarePlanRequest request = new CarePlanRequest();
        request.setPatientId(testPatientId);
        // title is missing
        request.setPriority(CarePlanPriority.MEDIUM);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusMonths(1));

        HttpEntity<CarePlanRequest> entity = new HttpEntity<>(request, createAuthHeaders(ownerToken));

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
    @DisplayName("Should return 500 when creating care plan with non-existent patient")
    void createCarePlan_PatientNotFound() {
        // ARRANGE
        CarePlanRequest request = new CarePlanRequest();
        request.setPatientId(UUID.randomUUID()); // Non-existent patient
        request.setTitle("Invalid Care Plan");
        request.setPriority(CarePlanPriority.MEDIUM);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusMonths(1));

        HttpEntity<CarePlanRequest> entity = new HttpEntity<>(request, createAuthHeaders(ownerToken));

        // ACT
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            entity,
            String.class
        );

        // ASSERT - Controller wraps exception causing 500
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should suspend active care plan successfully")
    void suspendCarePlan_Success() {
        // ARRANGE - Create and activate a care plan
        CarePlanRequest createRequest = new CarePlanRequest();
        createRequest.setPatientId(testPatientId);
        createRequest.setTitle("Plan to Suspend");
        createRequest.setPriority(CarePlanPriority.MEDIUM);
        createRequest.setStartDate(LocalDate.now());
        createRequest.setEndDate(LocalDate.now().plusMonths(2));
        createRequest.setAssignedCaregiverId(testCaregiverId);

        HttpEntity<CarePlanRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            createEntity,
            CarePlanResponse.class
        );
        
        UUID carePlanId = createResponse.getBody().getId();

        // Activate first
        HttpEntity<Void> activateEntity = new HttpEntity<>(createAuthHeaders(ownerToken));
        restTemplate.exchange(
            baseUrl + "/" + carePlanId + "/activate",
            HttpMethod.PUT,
            activateEntity,
            Void.class
        );

        // ACT - Suspend the care plan
        HttpEntity<Void> suspendEntity = new HttpEntity<>(createAuthHeaders(ownerToken));
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + carePlanId + "/suspend",
            HttpMethod.PUT,
            suspendEntity,
            Void.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify status changed to SUSPENDED
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> getResponse = restTemplate.exchange(
            baseUrl + "/" + carePlanId,
            HttpMethod.GET,
            getEntity,
            CarePlanResponse.class
        );

        assertThat(getResponse.getBody().getStatus()).isEqualTo(CarePlanStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Should complete active care plan successfully")
    void completeCarePlan_Success() {
        // ARRANGE - Create and activate a care plan
        CarePlanRequest createRequest = new CarePlanRequest();
        createRequest.setPatientId(testPatientId);
        createRequest.setTitle("Plan to Complete");
        createRequest.setPriority(CarePlanPriority.LOW);
        createRequest.setStartDate(LocalDate.now());
        createRequest.setEndDate(LocalDate.now().plusMonths(1));
        createRequest.setAssignedCaregiverId(testCaregiverId);

        HttpEntity<CarePlanRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> createResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            createEntity,
            CarePlanResponse.class
        );
        
        UUID carePlanId = createResponse.getBody().getId();

        // Activate first
        HttpEntity<Void> activateEntity = new HttpEntity<>(createAuthHeaders(ownerToken));
        restTemplate.exchange(
            baseUrl + "/" + carePlanId + "/activate",
            HttpMethod.PUT,
            activateEntity,
            Void.class
        );

        // ACT - Complete the care plan
        HttpEntity<Void> completeEntity = new HttpEntity<>(createAuthHeaders(ownerToken));
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + carePlanId + "/complete",
            HttpMethod.PUT,
            completeEntity,
            Void.class
        );

        // ASSERT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify status changed to COMPLETED
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders(ownerToken));
        ResponseEntity<CarePlanResponse> getResponse = restTemplate.exchange(
            baseUrl + "/" + carePlanId,
            HttpMethod.GET,
            getEntity,
            CarePlanResponse.class
        );

        assertThat(getResponse.getBody().getStatus()).isEqualTo(CarePlanStatus.COMPLETED);
    }
}