package com.geriatriccare.integration;

import com.geriatriccare.entity.Patient;
import com.geriatriccare.repository.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Patient API endpoints
 */
@DisplayName("Patient API Integration Tests")
class PatientIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Should create patient successfully")
    void createPatient_Success() throws Exception {
        String patientJson = """
            {
                "firstName": "Margaret",
                "lastName": "Thompson",
                "dateOfBirth": "1942-03-15",
                "medicalConditions": "Hypertension",
                "emergencyContact": "Sarah Thompson",
                "emergencyPhone": "+1-555-0124"
            }
            """;

        mockMvc.perform(post("/api/patients")
                .with(authenticatedUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(patientJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.firstName").value("Margaret"));

        assertThat(patientRepository.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should retrieve patient by ID")
    void getPatient_Success() throws Exception {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Anderson");
        patient.setDateOfBirth(LocalDate.of(1938, 6, 20));
        patient.setIsActive(true);
        patient = patientRepository.save(patient);

        mockMvc.perform(get("/api/patients/" + patient.getId())
                .with(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(patient.getId().toString()))
            .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("Should return 404 when patient not found")
    void getPatient_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/patients/" + nonExistentId)
                .with(authenticatedUser()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should list all patients")
    void listPatients_Success() throws Exception {
        Patient p1 = new Patient();
        p1.setFirstName("Patient1");
        p1.setLastName("Test1");
        p1.setDateOfBirth(LocalDate.of(1940, 1, 1));
        p1.setIsActive(true);
        patientRepository.save(p1);

        Patient p2 = new Patient();
        p2.setFirstName("Patient2");
        p2.setLastName("Test2");
        p2.setDateOfBirth(LocalDate.of(1950, 2, 2));
        p2.setIsActive(true);
        patientRepository.save(p2);

        mockMvc.perform(get("/api/patients")
                .with(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
