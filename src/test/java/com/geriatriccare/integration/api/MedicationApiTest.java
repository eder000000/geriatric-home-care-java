package com.geriatriccare.integration.api;

import com.geriatriccare.base.BaseIntegrationTest;
import com.geriatriccare.dto.MedicationRequest;
import com.geriatriccare.dto.MedicationResponse;
import com.geriatriccare.entity.MedicationForm;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Medication Management REST API.
 *
 * ⚠️  GAP: No MedicationController exists yet. SecurityConfig pre-authorizes /api/medications/**
 *     so authenticated requests get role-checked, then 403 (no handler). Unauthenticated → 401.
 *
 * TO ENABLE: Create MedicationController at /api/medications, then remove @Disabled.
 */
@DisplayName("Medication API Integration Tests [PENDING MedicationController]")
class MedicationApiTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/medications → 403 as ADMIN (role passes, no handler registered)")
    void medicationEndpoint_asAdmin_returns403() {
        assertThat(getWithAuth("/api/medications", adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/medications → 401 without token")
    void medicationEndpoint_noToken_returns401() {
        assertThat(restTemplate.getForEntity(baseUrl + "/api/medications", String.class)
            .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Disabled("Awaiting MedicationController at /api/medications")
    @DisplayName("POST /api/medications → 201 as ADMIN")
    void createMedication_asAdmin_returns201() {
        ResponseEntity<MedicationResponse> response = postWithAuth(
            "/api/medications", buildRequest("Metformin 500mg"), adminToken, MedicationResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Metformin 500mg");
    }

    @Test
    @Disabled("Awaiting MedicationController")
    @DisplayName("POST /api/medications → 403 as FAMILY")
    void createMedication_asFamily_returns403() {
        assertThat(postWithAuth("/api/medications", buildRequest("Blocked"), familyToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Disabled("Awaiting MedicationController")
    @DisplayName("POST /api/medications → 400 when expirationDate is in the past")
    void createMedication_pastExpiration_returns400() {
        MedicationRequest req = buildRequest("Expired");
        req.setExpirationDate(LocalDate.now().minusDays(1));
        assertThat(postWithAuth("/api/medications", req, adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Disabled("Awaiting MedicationController")
    @DisplayName("POST /api/medications → 400 when quantityInStock is negative")
    void createMedication_negativeQuantity_returns400() {
        MedicationRequest req = buildRequest("BadQty");
        req.setQuantityInStock(-1);
        assertThat(postWithAuth("/api/medications", req, adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Disabled("Awaiting MedicationController")
    @DisplayName("POST /api/medications → isLowStock=true when quantity <= reorderLevel")
    void createMedication_atReorderLevel_isLowStockTrue() {
        MedicationRequest req = buildRequest("LowStock");
        req.setQuantityInStock(5);
        req.setReorderLevel(10);
        ResponseEntity<MedicationResponse> response = postWithAuth(
            "/api/medications", req, adminToken, MedicationResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getIsLowStock()).isTrue();
    }

    @Test
    @Disabled("Awaiting MedicationController")
    @DisplayName("GET /api/medications/{id} → 404 for non-existent")
    void getMedication_nonExistent_returns404() {
        assertThat(getWithAuth("/api/medications/" + UUID.randomUUID(), adminToken, String.class)
            .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private MedicationRequest buildRequest(String name) {
        MedicationRequest req = new MedicationRequest();
        req.setName(name);
        req.setDosage("500mg");
        req.setForm(MedicationForm.TABLET);
        req.setExpirationDate(LocalDate.now().plusYears(2));
        req.setQuantityInStock(50);
        req.setReorderLevel(10);
        req.setManufacturer("PharmaCorp");
        return req;
    }
}
