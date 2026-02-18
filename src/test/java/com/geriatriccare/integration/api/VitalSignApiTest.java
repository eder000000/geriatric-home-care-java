package com.geriatriccare.integration.api;

import com.geriatriccare.base.BaseIntegrationTest;
import com.geriatriccare.dto.PatientRequest;
import com.geriatriccare.dto.PatientResponse;
import com.geriatriccare.dto.vitalsign.VitalSignRequest;
import com.geriatriccare.dto.vitalsign.VitalSignResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Vital Sign API Integration Tests")
class VitalSignApiTest extends BaseIntegrationTest {

    private UUID patientId;

    @BeforeEach
    void createTestPatient() {
        PatientRequest req = new PatientRequest();
        req.setFirstName("Vital");
        req.setLastName("TestPatient");
        req.setDateOfBirth(LocalDate.of(1940, 1, 1));
        patientId = postWithAuth("/api/patients", req, adminToken, PatientResponse.class)
                .getBody().getId();
    }

    @Test
    @DisplayName("GET /api/vital-signs/patient/{id} → 200 as ADMIN")
    void vitalSignEndpoint_asAdmin_returns200() {
        assertThat(getWithAuth("/api/vital-signs/patient/" + patientId, adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/vital-signs → 401 without token")
    void vitalSignEndpoint_noToken_returns401() {
        assertThat(restTemplate.getForEntity(baseUrl + "/api/vital-signs/patient/" + patientId, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/vital-signs → 201 with complete vitals as CAREGIVER")
    void recordVitalSign_complete_returns201() {
        ResponseEntity<VitalSignResponse> response = postWithAuth(
            "/api/vital-signs", buildRequest(patientId), caregiverToken, VitalSignResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getPatientId()).isEqualTo(patientId);
        assertThat(response.getBody().getBloodPressureSystolic()).isEqualTo(125);
    }

    @Test
    @DisplayName("POST /api/vital-signs → 403 as FAMILY")
    void recordVitalSign_asFamily_returns403() {
        assertThat(postWithAuth("/api/vital-signs", buildRequest(patientId), familyToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("POST /api/vital-signs → 400 when systolicBP > 250")
    void recordVitalSign_invalidSystolicBP_returns400() {
        VitalSignRequest req = buildRequest(patientId);
        req.setBloodPressureSystolic(999);
        assertThat(postWithAuth("/api/vital-signs", req, caregiverToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/vital-signs → 400 when oxygenSaturation > 100")
    void recordVitalSign_invalidOxygen_returns400() {
        VitalSignRequest req = buildRequest(patientId);
        req.setOxygenSaturation(101);
        assertThat(postWithAuth("/api/vital-signs", req, caregiverToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/vital-signs/{id} → 404 for non-existent")
    void getVitalSign_nonExistent_returns404() {
        assertThat(getWithAuth("/api/vital-signs/" + UUID.randomUUID(), adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/vital-signs/patient/{patientId} → 200 returns history")
    void getPatientVitalSigns_returnsHistory() {
        assertThat(getWithAuth("/api/vital-signs/patient/" + patientId, caregiverToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private VitalSignRequest buildRequest(UUID patientId) {
        VitalSignRequest req = new VitalSignRequest();
        req.setPatientId(patientId);
        req.setBloodPressureSystolic(125);
        req.setBloodPressureDiastolic(80);
        req.setHeartRate(72);
        req.setTemperature(36.6);
        req.setOxygenSaturation(98);
        return req;
    }
}
