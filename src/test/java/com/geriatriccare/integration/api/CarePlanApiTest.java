package com.geriatriccare.integration.api;

import com.geriatriccare.base.BaseIntegrationTest;
import com.geriatriccare.dto.*;
import com.geriatriccare.entity.CarePlanPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Care Plan API Integration Tests")
class CarePlanApiTest extends BaseIntegrationTest {

    private UUID testPatientId;

    @BeforeEach
    void setUpCarePlan() {
        PatientRequest patientReq = new PatientRequest();
        patientReq.setFirstName("CarePlan");
        patientReq.setLastName("TestPatient");
        patientReq.setDateOfBirth(LocalDate.of(1940, 1, 1));
        testPatientId = postWithAuth("/api/patients", patientReq, adminToken, PatientResponse.class)
                .getBody().getId();
    }

    @Test
    @DisplayName("POST /api/care-plans → 201 as ADMIN")
    void createCarePlan_Success() {
        ResponseEntity<CarePlanResponse> response = postWithAuth(
                "/api/care-plans", buildRequest(), adminToken, CarePlanResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getTitle()).isEqualTo("Test Care Plan");
        assertThat(response.getBody().getPatientId()).isEqualTo(testPatientId);
    }

    @Test
    @DisplayName("GET /api/care-plans/{id} → 200 returns care plan")
    void getCarePlan_Success() {
        UUID id = createCarePlan();
        ResponseEntity<CarePlanResponse> response = getWithAuth(
                "/api/care-plans/" + id, adminToken, CarePlanResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("PUT /api/care-plans/{id} → 200 updates care plan")
    void updateCarePlan_Success() {
        UUID id = createCarePlan();
        CarePlanRequest update = buildRequest();
        update.setTitle("Updated Plan");

        ResponseEntity<CarePlanResponse> response = putWithAuth(
                "/api/care-plans/" + id, update, adminToken, CarePlanResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Plan");
    }

    @Test
    @DisplayName("PUT /api/care-plans/{id}/activate → 204 activates care plan")
    void activateCarePlan_Success() {
        UUID id = createCarePlan();
        ResponseEntity<Void> response = putWithAuth(
                "/api/care-plans/" + id + "/activate", null, adminToken, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("PUT /api/care-plans/{id}/suspend → 204 suspends care plan")
    void suspendCarePlan_Success() {
        UUID id = createCarePlan();
        putWithAuth("/api/care-plans/" + id + "/activate", null, adminToken, Void.class);

        ResponseEntity<Void> response = putWithAuth(
                "/api/care-plans/" + id + "/suspend", null, adminToken, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("PUT /api/care-plans/{id}/complete → 204 completes care plan")
    void completeCarePlan_Success() {
        UUID id = createCarePlan();
        putWithAuth("/api/care-plans/" + id + "/activate", null, adminToken, Void.class);

        ResponseEntity<Void> response = putWithAuth(
                "/api/care-plans/" + id + "/complete", null, adminToken, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("DELETE /api/care-plans/{id} → 204 deletes care plan")
    void deleteCarePlan_Success() {
        UUID id = createCarePlan();
        ResponseEntity<Void> response = deleteWithAuth(
                "/api/care-plans/" + id, adminToken, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /api/care-plans?patientId={id} → 200 returns list")
    void getPatientCarePlans_Success() {
        createCarePlan();
        ResponseEntity<String> response = getWithAuth(
                "/api/care-plans?patientId=" + testPatientId, adminToken, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("POST /api/care-plans → 403 as FAMILY")
    void createCarePlan_asFamily_returns403() {
        assertThat(postWithAuth("/api/care-plans", buildRequest(), familyToken, String.class)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private UUID createCarePlan() {
        return postWithAuth("/api/care-plans", buildRequest(), adminToken, CarePlanResponse.class)
                .getBody().getId();
    }

    private CarePlanRequest buildRequest() {
        CarePlanRequest req = new CarePlanRequest();
        req.setTitle("Test Care Plan");
        req.setDescription("Integration test care plan");
        req.setPatientId(testPatientId);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusMonths(3));
        req.setPriority(CarePlanPriority.MEDIUM);
        return req;
    }
}
