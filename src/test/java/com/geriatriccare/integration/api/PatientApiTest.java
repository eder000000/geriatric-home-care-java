package com.geriatriccare.integration.api;

import com.geriatriccare.base.BaseIntegrationTest;
import com.geriatriccare.dto.PatientRequest;
import com.geriatriccare.dto.PatientResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Patient Management (Epic 3).
 *
 * PatientRequest fields: firstName, lastName, dateOfBirth (@Past), medicalConditions,
 *                        emergencyContact, emergencyPhone
 * PatientResponse fields: id (UUID), firstName, lastName, dateOfBirth, isActive,
 *                         age, fullName, medicalConditions, createdAt
 *
 * Role access (from @PreAuthorize on PatientController):
 *   POST   → ADMIN, PHYSICIAN, CAREGIVER
 *   GET    → ADMIN, PHYSICIAN, CAREGIVER, FAMILY
 *   PUT    → ADMIN, PHYSICIAN, CAREGIVER
 *   DELETE → ADMIN only
 *   GET all (paginated) → ADMIN, PHYSICIAN only
 */
@DisplayName("Patient API Integration Tests")
class PatientApiTest extends BaseIntegrationTest {

    // ─── Create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/patients → 201 as ADMIN")
    void createPatient_asAdmin_returns201() {
        ResponseEntity<PatientResponse> response = postWithAuth(
            "/api/patients", buildRequest("Lucía"), adminToken, PatientResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("Lucía");
        assertThat(response.getBody().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("POST /api/patients → 201 as PHYSICIAN")
    void createPatient_asPhysician_returns201() {
        assertThat(postWithAuth("/api/patients", buildRequest("Elena"), physicianToken,
            PatientResponse.class).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/patients → 201 as CAREGIVER")
    void createPatient_asCaregiver_returns201() {
        assertThat(postWithAuth("/api/patients", buildRequest("Rosa"), caregiverToken,
            PatientResponse.class).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("POST /api/patients → 403 as FAMILY")
    void createPatient_asFamily_returns403() {
        assertThat(postWithAuth("/api/patients", buildRequest("Blocked"), familyToken,
            String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("POST /api/patients → 401 without token")
    void createPatient_noToken_returns401() {
        assertThat(postNoAuth("/api/patients", buildRequest("NoAuth"))
            .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/patients → 400 when firstName is missing")
    void createPatient_missingFirstName_returns400() {
        PatientRequest req = buildRequest(null);
        req.setFirstName(null);
        assertThat(postWithAuth("/api/patients", req, adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/patients → 400 when dateOfBirth is in the future")
    void createPatient_futureDOB_returns400() {
        PatientRequest req = buildRequest("Future");
        req.setDateOfBirth(LocalDate.now().plusYears(1));
        assertThat(postWithAuth("/api/patients", req, adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/patients/{id} → 200 returns patient for ADMIN")
    void getPatient_asAdmin_returns200() {
        UUID id = createPatient("Carlos");
        ResponseEntity<PatientResponse> response = getWithAuth(
            "/api/patients/" + id, adminToken, PatientResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getFirstName()).isEqualTo("Carlos");
    }

    @Test
    @DisplayName("GET /api/patients/{id} → 200 for FAMILY role")
    void getPatient_asFamily_returns200() {
        UUID id = createPatient("Visible");
        assertThat(getWithAuth("/api/patients/" + id, familyToken, PatientResponse.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/patients/{id} → 404 for non-existent patient")
    void getPatient_nonExistent_returns404() {
        assertThat(getWithAuth("/api/patients/" + UUID.randomUUID(), adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/patients → 200 paginated list for ADMIN")
    void listPatients_asAdmin_returnsPagedResult() {
        createPatient("Uno");
        createPatient("Dos");
        ResponseEntity<String> response = getWithAuth("/api/patients", adminToken, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content"); // Spring Page JSON
    }

    @Test
    @DisplayName("GET /api/patients → 403 for CAREGIVER (only ADMIN, PHYSICIAN can list all)")
    void listPatients_asCaregiver_returns403() {
        assertThat(getWithAuth("/api/patients", caregiverToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/patients/{id} → 200 updates medical conditions")
    void updatePatient_medicalConditions_returns200() {
        UUID id = createPatient("UpdateMe");
        PatientRequest update = buildRequest("UpdateMe");
        update.setMedicalConditions("Hypertension, Arthritis, Type 2 Diabetes");

        ResponseEntity<PatientResponse> response = putWithAuth(
            "/api/patients/" + id, update, physicianToken, PatientResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMedicalConditions()).contains("Arthritis");
    }

    @Test
    @DisplayName("PUT /api/patients/{id} → 404 for non-existent patient")
    void updatePatient_nonExistent_returns404() {
        assertThat(putWithAuth("/api/patients/" + UUID.randomUUID(),
            buildRequest("Ghost"), adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/patients/{id} → 204 as ADMIN")
    void deletePatient_asAdmin_returns204() {
        UUID id = createPatient("ToDelete");
        assertThat(deleteWithAuth("/api/patients/" + id, adminToken, Void.class)
            .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} → 403 as PHYSICIAN")
    void deletePatient_asPhysician_returns403() {
        UUID id = createPatient("Protected");
        assertThat(deleteWithAuth("/api/patients/" + id, physicianToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── Search / Utilities ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/patients/search/name?name=García → 200 returns matches")
    void searchByName_returnsMatchingPatients() {
        createPatient("María");
        ResponseEntity<String> response = getWithAuth(
            "/api/patients/search/name?name=García", adminToken, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("García");
    }

    @Test
    @DisplayName("GET /api/patients/count → 200 returns active patient count")
    void getPatientCount_returnsLong() {
        createPatient("Count1");
        createPatient("Count2");
        ResponseEntity<Long> response = getWithAuth(
            "/api/patients/count", adminToken, Long.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isGreaterThanOrEqualTo(2L);
    }

    @Test
    @DisplayName("GET /api/patients/{id}/exists → 200 true for existing patient")
    void patientExists_returnsTrue() {
        UUID id = createPatient("Exists");
        ResponseEntity<Boolean> response = getWithAuth(
            "/api/patients/" + id + "/exists", caregiverToken, Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private UUID createPatient(String firstName) {
        ResponseEntity<PatientResponse> r = postWithAuth(
            "/api/patients", buildRequest(firstName), adminToken, PatientResponse.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return r.getBody().getId();
    }

    private PatientRequest buildRequest(String firstName) {
        PatientRequest req = new PatientRequest();
        req.setFirstName(firstName);
        req.setLastName("García");
        req.setDateOfBirth(LocalDate.of(1945, 7, 20));
        req.setMedicalConditions("Hypertension, Type 2 Diabetes");
        req.setEmergencyContact("Carlos García");
        req.setEmergencyPhone("+52-33-1234-5678");
        return req;
    }
}
