package com.geriatriccare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geriatriccare.dto.patient.PatientRequest;
import com.geriatriccare.model.Patient;
import com.geriatriccare.model.User;
import com.geriatriccare.model.enums.UserRole;
import com.geriatriccare.repository.PatientRepository;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Patient API endpoints
 * Tests CRUD operations with proper authorization
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PatientApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User caregiverUser;
    private User familyUser;
    private Patient testPatient;
    private String caregiverToken;
    private String familyToken;

    @BeforeEach
    void setUp() {
        // Clean up
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Create caregiver user
        caregiverUser = new User();
        caregiverUser.setFirstName("Caregiver");
        caregiverUser.setLastName("Test");
        caregiverUser.setEmail("caregiver@test.com");
        caregiverUser.setPassword(passwordEncoder.encode("Test123!@#"));
        caregiverUser.setRole(UserRole.CAREGIVER);
        caregiverUser.setIsActive(true);
        caregiverUser = userRepository.save(caregiverUser);
        caregiverToken = jwtUtil.generateToken(caregiverUser.getEmail(), caregiverUser.getRole().name());

        // Create family user
        familyUser = new User();
        familyUser.setFirstName("Family");
        familyUser.setLastName("Member");
        familyUser.setEmail("family@test.com");
        familyUser.setPassword(passwordEncoder.encode("Test123!@#"));
        familyUser.setRole(UserRole.FAMILY);
        familyUser.setIsActive(true);
        familyUser = userRepository.save(familyUser);
        familyToken = jwtUtil.generateToken(familyUser.getEmail(), familyUser.getRole().name());

        // Create test patient
        testPatient = new Patient();
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setDateOfBirth(LocalDate.of(1945, 5, 15));
        testPatient.setGender("Male");
        testPatient.setPhone("555-1234");
        testPatient.setEmail("john.doe@example.com");
        testPatient.setAddress("123 Main St, City, State 12345");
        testPatient.setMedicalHistory("Hypertension, Type 2 Diabetes");
        testPatient.setCurrentMedications("Metformin 500mg, Lisinopril 10mg");
        testPatient.setAllergies("Penicillin");
        testPatient.setEmergencyContactName("Jane Doe");
        testPatient.setEmergencyContactPhone("555-5678");
        testPatient.setEmergencyContactRelationship("Spouse");
        testPatient.setInsuranceProvider("Medicare");
        testPatient.setInsurancePolicyNumber("MCARE123456");
        testPatient = patientRepository.save(testPatient);
    }

    @Test
    @DisplayName("POST /api/patients - Caregiver should create patient successfully")
    void createPatient_AsCaregiver_Success() throws Exception {
        PatientRequest request = new PatientRequest();
        request.setFirstName("Sarah");
        request.setLastName("Smith");
        request.setDateOfBirth(LocalDate.of(1950, 3, 20));
        request.setGender("Female");
        request.setPhone("555-9999");
        request.setEmail("sarah.smith@example.com");
        request.setAddress("456 Oak Ave, City, State 12345");
        request.setEmergencyContactName("Robert Smith");
        request.setEmergencyContactPhone("555-8888");
        request.setEmergencyContactRelationship("Son");

        mockMvc.perform(post("/api/patients")
                .header("Authorization", "Bearer " + caregiverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Sarah"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("sarah.smith@example.com"))
                .andExpect(jsonPath("$.age").exists());
    }

    @Test
    @DisplayName("POST /api/patients - Family member should not be able to create patient")
    void createPatient_AsFamily_Forbidden() throws Exception {
        PatientRequest request = new PatientRequest();
        request.setFirstName("Unauthorized");
        request.setLastName("Patient");
        request.setDateOfBirth(LocalDate.of(1950, 1, 1));
        request.setGender("Male");
        request.setPhone("555-0000");

        mockMvc.perform(post("/api/patients")
                .header("Authorization", "Bearer " + familyToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/patients - Should return all patients for caregiver")
    void getAllPatients_AsCaregiver_Success() throws Exception {
        mockMvc.perform(get("/api/patients")
                .header("Authorization", "Bearer " + caregiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].firstName").exists());
    }

    @Test
    @DisplayName("GET /api/patients/{id} - Should return patient details")
    void getPatientById_Success() throws Exception {
        mockMvc.perform(get("/api/patients/" + testPatient.getId())
                .header("Authorization", "Bearer " + caregiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPatient.getId().toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.medicalHistory").value("Hypertension, Type 2 Diabetes"))
                .andExpect(jsonPath("$.allergies").value("Penicillin"));
    }

    @Test
    @DisplayName("GET /api/patients/{id} - Should return 404 for non-existent patient")
    void getPatientById_NotFound() throws Exception {
        mockMvc.perform(get("/api/patients/00000000-0000-0000-0000-000000000000")
                .header("Authorization", "Bearer " + caregiverToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/patients/{id} - Should update patient successfully")
    void updatePatient_Success() throws Exception {
        PatientRequest request = new PatientRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(testPatient.getDateOfBirth());
        request.setGender("Male");
        request.setPhone("555-9999"); // Updated phone
        request.setEmail("newemail@example.com"); // Updated email
        request.setAddress(testPatient.getAddress());
        request.setMedicalHistory("Hypertension, Type 2 Diabetes, Osteoarthritis"); // Updated
        request.setCurrentMedications(testPatient.getCurrentMedications());
        request.setAllergies(testPatient.getAllergies());
        request.setEmergencyContactName(testPatient.getEmergencyContactName());
        request.setEmergencyContactPhone(testPatient.getEmergencyContactPhone());
        request.setEmergencyContactRelationship(testPatient.getEmergencyContactRelationship());

        mockMvc.perform(put("/api/patients/" + testPatient.getId())
                .header("Authorization", "Bearer " + caregiverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("555-9999"))
                .andExpect(jsonPath("$.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.medicalHistory").value(containsString("Osteoarthritis")));
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} - Should soft delete patient")
    void deletePatient_Success() throws Exception {
        mockMvc.perform(delete("/api/patients/" + testPatient.getId())
                .header("Authorization", "Bearer " + caregiverToken))
                .andExpect(status().isNoContent());

        // Verify patient is soft deleted (still exists but inactive)
        mockMvc.perform(get("/api/patients/" + testPatient.getId())
                .header("Authorization", "Bearer " + caregiverToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/patients/search - Should search patients by name")
    void searchPatients_ByName_Success() throws Exception {
        mockMvc.perform(get("/api/patients/search")
                .param("query", "John")
                .header("Authorization", "Bearer " + caregiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    @DisplayName("GET /api/patients/{id}/medical-summary - Should return medical summary")
    void getMedicalSummary_Success() throws Exception {
        mockMvc.perform(get("/api/patients/" + testPatient.getId() + "/medical-summary")
                .header("Authorization", "Bearer " + caregiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("John Doe"))
                .andExpect(jsonPath("$.age").exists())
                .andExpect(jsonPath("$.medicalHistory").exists())
                .andExpect(jsonPath("$.currentMedications").exists())
                .andExpect(jsonPath("$.allergies").exists());
    }

    @Test
    @DisplayName("POST /api/patients - Should reject invalid email format")
    void createPatient_InvalidEmail() throws Exception {
        PatientRequest request = new PatientRequest();
        request.setFirstName("Invalid");
        request.setLastName("Email");
        request.setDateOfBirth(LocalDate.of(1950, 1, 1));
        request.setGender("Male");
        request.setPhone("555-1111");
        request.setEmail("invalid-email"); // Invalid format

        mockMvc.perform(post("/api/patients")
                .header("Authorization", "Bearer " + caregiverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/patients - Should reject missing required fields")
    void createPatient_MissingRequiredFields() throws Exception {
        PatientRequest request = new PatientRequest();
        // Missing firstName, lastName, dateOfBirth

        mockMvc.perform(post("/api/patients")
                .header("Authorization", "Bearer " + caregiverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/patients - Should require authentication")
    void getAllPatients_NoAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }
}