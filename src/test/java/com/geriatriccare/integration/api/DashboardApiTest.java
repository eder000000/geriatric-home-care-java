package com.geriatriccare.integration.api;

import com.geriatriccare.base.BaseIntegrationTest;
import com.geriatriccare.dto.PatientRequest;
import com.geriatriccare.dto.PatientResponse;
import com.geriatriccare.dto.dashboard.DashboardRequest;
import com.geriatriccare.dto.dashboard.DashboardStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Dashboard (Epic 8 — DashboardController).
 *
 * DashboardController endpoints:
 *   GET  /api/dashboard/statistics  (no body — uses defaults)
 *   POST /api/dashboard/statistics  (optional DashboardRequest body; null handled by controller)
 *
 * Role access: ADMIN and PHYSICIAN only.
 */
@DisplayName("Dashboard API Integration Tests")
class DashboardApiTest extends BaseIntegrationTest {

    // ─── GET endpoint ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/dashboard/statistics → 200 for ADMIN")
    void getStatistics_asAdmin_returns200() {
        ResponseEntity<DashboardStatistics> response = getWithAuth(
            "/api/dashboard/statistics", adminToken, DashboardStatistics.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/dashboard/statistics → 200 for PHYSICIAN")
    void getStatistics_asPhysician_returns200() {
        assertThat(getWithAuth("/api/dashboard/statistics", physicianToken,
            DashboardStatistics.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/dashboard/statistics → 403 for CAREGIVER")
    void getStatistics_asCaregiver_returns403() {
        assertThat(getWithAuth("/api/dashboard/statistics", caregiverToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /api/dashboard/statistics → 403 for FAMILY")
    void getStatistics_asFamily_returns403() {
        assertThat(getWithAuth("/api/dashboard/statistics", familyToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /api/dashboard/statistics → 401 without token")
    void getStatistics_noToken_returns401() {
        assertThat(restTemplate.getForEntity(
            baseUrl + "/api/dashboard/statistics", String.class)
            .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── POST endpoint ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/dashboard/statistics → 200 with empty request body")
    void postStatistics_emptyBody_returns200() {
        ResponseEntity<DashboardStatistics> response = postWithAuth(
            "/api/dashboard/statistics", new DashboardRequest(), adminToken, DashboardStatistics.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/dashboard/statistics → 200 with null body (controller handles null)")
    void postStatistics_nullBody_returns200() {
        // Controller has: if (request == null) { request = new DashboardRequest(); }
        ResponseEntity<DashboardStatistics> response = postWithAuth(
            "/api/dashboard/statistics", null, adminToken, DashboardStatistics.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("POST /api/dashboard/statistics → 403 for CAREGIVER")
    void postStatistics_asCaregiver_returns403() {
        assertThat(postWithAuth("/api/dashboard/statistics", new DashboardRequest(),
            caregiverToken, String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── Data consistency ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/dashboard/statistics → non-null response after creating a patient")
    void getStatistics_afterCreatingPatient_returnsData() {
        PatientRequest req = new PatientRequest();
        req.setFirstName("Dashboard");
        req.setLastName("Patient");
        req.setDateOfBirth(LocalDate.of(1942, 6, 10));
        postWithAuth("/api/patients", req, adminToken, PatientResponse.class);

        ResponseEntity<DashboardStatistics> response = getWithAuth(
            "/api/dashboard/statistics", adminToken, DashboardStatistics.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
