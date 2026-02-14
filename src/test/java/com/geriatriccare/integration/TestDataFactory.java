package com.geriatriccare.integration;

import com.geriatriccare.entity.Patient;
import com.geriatriccare.entity.User;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.enums.UserStatus;
import com.geriatriccare.repository.PatientRepository;
import com.geriatriccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Factory for creating test data with all required fields
 * Handles unique constraints and relationships
 */
@Component
public class TestDataFactory {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    private int userCounter = 0;
    private int patientCounter = 0;

    /**
     * Create a test user with all required fields
     */
    public User createTestUser(UserRole role) {
        userCounter++;
        
        User user = new User();
        user.setUsername("testuser" + userCounter);
        user.setEmail("test" + userCounter + "@test.com");
        user.setPassword("hashedpassword");
        user.setFirstName("Test");
        user.setLastName("User" + userCounter);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        
        return userRepository.save(user);
    }

    /**
     * Create test admin user
     */
    public User createAdminUser() {
        return createTestUser(UserRole.ADMIN);
    }

    /**
     * Create test physician user
     */
    public User createPhysicianUser() {
        return createTestUser(UserRole.PHYSICIAN);
    }

    /**
     * Create a test patient
     */
    public Patient createTestPatient() {
        patientCounter++;
        
        Patient patient = new Patient();
        patient.setFirstName("Patient");
        patient.setLastName("Test" + patientCounter);
        patient.setDateOfBirth(LocalDate.of(1940, 1, 1).plusDays(patientCounter));
        patient.setIsActive(true);
        
        return patientRepository.save(patient);
    }

    /**
     * Reset counters (call between tests if needed)
     */
    public void reset() {
        userCounter = 0;
        patientCounter = 0;
    }
}
