// File: src/test/java/com/geriatriccare/unit/service/PatientServiceTest.java
package com.geriatriccare.unit.service;

import com.geriatriccare.builders.PatientTestBuilder;
import com.geriatriccare.dto.PatientRequest;
import com.geriatriccare.dto.PatientResponse;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.repository.PatientCaregiverRepository;
import com.geriatriccare.repository.PatientFamilyMemberRepository;
import com.geriatriccare.repository.PatientRepository;
import com.geriatriccare.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PatientServiceTest {
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private PatientCaregiverRepository patientCaregiverRepository;
    
    @Mock
    private PatientFamilyMemberRepository patientFamilyMemberRepository;
    
    @InjectMocks
    private PatientService patientService;
    
    private Patient testPatient;
    private PatientRequest patientRequest;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testPatient = PatientTestBuilder.aPatient()
            .withName("John", "Doe")
            .withAge(75)
            .diabetic()
            .build();
        testPatient.setId(UUID.randomUUID());
        
        patientRequest = new PatientRequest();
        patientRequest.setFirstName("Jane");
        patientRequest.setLastName("Smith");
        patientRequest.setDateOfBirth(LocalDate.of(1948, 5, 15));
        patientRequest.setMedicalConditions("Hypertension");
        patientRequest.setEmergencyContact("John Smith");
        patientRequest.setEmergencyPhone("555-1234");
    }
    
    @Test
    @DisplayName("Should create patient successfully")
    void createPatient_Success() {
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        when(patientCaregiverRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        when(patientFamilyMemberRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        
        PatientResponse response = patientService.createPatient(patientRequest);
        
        assertThat(response).isNotNull();
        verify(patientRepository).save(any(Patient.class));
    }
    
    @Test
    @DisplayName("Should return patient when found by ID")
    void getPatientById_Found() {
        UUID patientId = testPatient.getId();
        when(patientRepository.findByIdAndIsActiveTrue(patientId))
            .thenReturn(Optional.of(testPatient));
        when(patientCaregiverRepository.findByPatientIdAndIsActiveTrue(patientId))
            .thenReturn(Collections.emptyList());
        when(patientFamilyMemberRepository.findByPatientIdAndIsActiveTrue(patientId))
            .thenReturn(Collections.emptyList());
        
        Optional<PatientResponse> result = patientService.getPatientById(patientId);
        
        assertThat(result).isPresent();
        verify(patientRepository).findByIdAndIsActiveTrue(patientId);
    }
    
    @Test
    @DisplayName("Should return empty when patient not found")
    void getPatientById_NotFound() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findByIdAndIsActiveTrue(patientId))
            .thenReturn(Optional.empty());
        
        Optional<PatientResponse> result = patientService.getPatientById(patientId);
        
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should return paginated patients")
    void getAllPatients_Paginated() {
        Patient patient2 = PatientTestBuilder.aPatient().withName("Jane", "Smith").build();
        patient2.setId(UUID.randomUUID());
        
        List<Patient> patients = Arrays.asList(testPatient, patient2);
        Page<Patient> patientPage = new PageImpl<>(patients, PageRequest.of(0, 10), patients.size());
        
        when(patientRepository.findAll(any(Pageable.class))).thenReturn(patientPage);
        when(patientCaregiverRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        when(patientFamilyMemberRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        
        Page<PatientResponse> result = patientService.getAllPatients(0, 10, "firstName", "asc");
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }
    
    @Test
    @DisplayName("Should update patient successfully")
    void updatePatient_Success() {
        UUID patientId = testPatient.getId();
        
        when(patientRepository.findByIdAndIsActiveTrue(patientId))
            .thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        when(patientCaregiverRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        when(patientFamilyMemberRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        
        PatientResponse response = patientService.updatePatient(patientId, patientRequest);
        
        assertThat(response).isNotNull();
        verify(patientRepository).save(any(Patient.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating non-existent patient")
    void updatePatient_NotFound() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findByIdAndIsActiveTrue(patientId))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> patientService.updatePatient(patientId, patientRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Patient not found");
        
        verify(patientRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should soft delete patient")
    void deletePatient_Success() {
        UUID patientId = testPatient.getId();
        when(patientRepository.findByIdAndIsActiveTrue(patientId))
            .thenReturn(Optional.of(testPatient));
        
        patientService.deletePatient(patientId);
        
        verify(patientRepository).save(testPatient);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting non-existent patient")
    void deletePatient_NotFound() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findByIdAndIsActiveTrue(patientId))
            .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> patientService.deletePatient(patientId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Patient not found");
    }
    
    @Test
    @DisplayName("Should search patients by name")
    void searchPatientsByName() {
        String searchName = "John";
        List<Patient> patients = Arrays.asList(testPatient);
        
        when(patientRepository.findByNameContainingIgnoreCase(searchName))
            .thenReturn(patients);
        when(patientCaregiverRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        when(patientFamilyMemberRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        
        List<PatientResponse> results = patientService.searchPatientsByName(searchName);
        
        assertThat(results).hasSize(1);
    }
    
    @Test
    @DisplayName("Should search patients by age range")
    void searchPatientsByAgeRange() {
        List<Patient> patients = Arrays.asList(testPatient);
        
        when(patientRepository.findByAgeBetween(anyInt(), anyInt()))
            .thenReturn(patients);
        when(patientCaregiverRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        when(patientFamilyMemberRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        
        List<PatientResponse> results = patientService.searchPatientsByAgeRange(70, 80);
        
        assertThat(results).hasSize(1);
    }
    
    @Test
    @DisplayName("Should search patients by medical condition")
    void searchPatientsByCondition() {
        String condition = "Diabetes";
        List<Patient> patients = Arrays.asList(testPatient);
        
        when(patientRepository.findByMedicalConditionsContaining(condition))
            .thenReturn(patients);
        when(patientCaregiverRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        when(patientFamilyMemberRepository.findByPatientIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Collections.emptyList());
        
        List<PatientResponse> results = patientService.searchPatientsByCondition(condition);
        
        assertThat(results).hasSize(1);
    }
    
    @Test
    @DisplayName("Should check if patient exists by ID")
    void existsById() {
        UUID existingId = UUID.randomUUID();
        UUID nonExistingId = UUID.randomUUID();
        
        when(patientRepository.findByIdAndIsActiveTrue(existingId))
            .thenReturn(Optional.of(testPatient));
        when(patientRepository.findByIdAndIsActiveTrue(nonExistingId))
            .thenReturn(Optional.empty());
        
        assertThat(patientService.existsById(existingId)).isTrue();
        assertThat(patientService.existsById(nonExistingId)).isFalse();
    }
    
    @Test
    @DisplayName("Should get total count of active patients")
    void getTotalActivePatients() {
        when(patientRepository.countActivePatients()).thenReturn(42L);
        
        long actualCount = patientService.getTotalActivePatients();
        
        assertThat(actualCount).isEqualTo(42L);
    }
} 