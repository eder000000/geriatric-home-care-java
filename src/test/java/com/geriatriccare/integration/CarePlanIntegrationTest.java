package com.geriatriccare.integration;

import com.geriatriccare.entity.CarePlan;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.entity.User;
import com.geriatriccare.repository.CarePlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Care Plan API Integration Tests")
class CarePlanIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarePlanRepository carePlanRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private Patient testPatient;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createAdminUser();
        testPatient = testDataFactory.createTestPatient();
    }

    @Test
    @DisplayName("Should create care plan successfully")
    @WithMockUser(username = "testuser1", roles = {"ADMIN"})
    void createCarePlan_Success() throws Exception {
        String carePlanJson = """
            {
                "patientId": "%s",
                "title": "Daily Care Plan",
                "description": "Comprehensive daily care",
                "startDate": "2026-02-01",
                "priority": "MEDIUM"
            }
            """.formatted(testPatient.getId());

        mockMvc.perform(post("/api/care-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(carePlanJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Daily Care Plan"));

        assertThat(carePlanRepository.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should retrieve care plan by ID")
    @WithMockUser(username = "testuser2", roles = {"ADMIN"})
    void getCarePlan_Success() throws Exception {
        User user2 = testDataFactory.createAdminUser();
        
        CarePlan carePlan = new CarePlan();
        carePlan.setPatient(testPatient);
        carePlan.setCreatedBy(user2);
        carePlan.setTitle("Test Care Plan");
        carePlan.setStartDate(LocalDate.now());
        carePlan = carePlanRepository.save(carePlan);

        mockMvc.perform(get("/api/care-plans/" + carePlan.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(carePlan.getId().toString()))
            .andExpect(jsonPath("$.title").value("Test Care Plan"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent care plan")
    @WithMockUser(username = "testuser3", roles = {"ADMIN"})
    void getCarePlan_NotFound() throws Exception {
        testDataFactory.createAdminUser();  // Create user3
        
        mockMvc.perform(get("/api/care-plans/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should list all care plans")
    @WithMockUser(username = "testuser4", roles = {"ADMIN"})
    void listCarePlans_Success() throws Exception {
        User user4 = testDataFactory.createAdminUser();
        
        CarePlan cp1 = new CarePlan();
        cp1.setPatient(testPatient);
        cp1.setCreatedBy(user4);
        cp1.setTitle("Plan 1");
        cp1.setStartDate(LocalDate.now());
        carePlanRepository.save(cp1);

        mockMvc.perform(get("/api/care-plans"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
