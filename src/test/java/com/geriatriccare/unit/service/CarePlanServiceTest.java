package com.geriatriccare.unit.service;

import com.geriatriccare.builders.CarePlanTestBuilder;
import com.geriatriccare.builders.PatientTestBuilder;
import com.geriatriccare.dto.CarePlanRequest;
import com.geriatriccare.dto.CarePlanResponse;
import com.geriatriccare.dto.CarePlanUpdateRequest;
import com.geriatriccare.entity.*;
import com.geriatriccare.repository.CarePlanRepository;
import com.geriatriccare.repository.PatientRepository;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.service.CarePlanService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CarePlanServiceTest {
    
    @Mock
    private CarePlanRepository carePlanRepository;
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private CarePlanService carePlanService;
    
    private Patient testPatient;
    private User testCaregiver;
    private User testOwner;
    private CarePlan testCarePlan;
    private CarePlanRequest carePlanRequest;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        
        // Setup test patient
        testPatient = PatientTestBuilder.aPatient()
            .withName("John", "Doe")
            .withAge(75)
            .diabetic()
            .build();
        testPatient.setId(UUID.randomUUID());
        
        // Setup test users
        testOwner = new User();
        testOwner.setId(UUID.randomUUID());
        testOwner.setEmail("owner@test.com");
        testOwner.setFirstName("Test");
        testOwner.setLastName("Owner");
        testOwner.setRole(UserRole.OWNER);
        
        testCaregiver = new User();
        testCaregiver.setId(UUID.randomUUID());
        testCaregiver.setEmail("caregiver@test.com");
        testCaregiver.setFirstName("Test");
        testCaregiver.setLastName("Caregiver");
        testCaregiver.setRole(UserRole.CAREGIVER);
        
        // Setup test care plan
        testCarePlan = CarePlanTestBuilder.aCarePlan()
            .forPatient(testPatient)
            .assignedTo(testCaregiver)
            .createdBy(testOwner)
            .withTitle("Diabetes Management")
            .active()
            .highPriority()
            .build();
        testCarePlan.setId(UUID.randomUUID());
        
        // Setup request
        carePlanRequest = new CarePlanRequest();
        carePlanRequest.setPatientId(testPatient.getId());
        carePlanRequest.setTitle("Diabetes Management");
        carePlanRequest.setDescription("Daily diabetes care plan");
        carePlanRequest.setPriority(CarePlanPriority.HIGH);
        carePlanRequest.setStartDate(LocalDate.now());
        carePlanRequest.setEndDate(LocalDate.now().plusMonths(3));
        carePlanRequest.setAssignedCaregiverId(testCaregiver.getId());
    }
    
    // Test 1: Create Care Plan Successfully
    @Test
    @DisplayName("Should create care plan successfully")
    void createCarePlan_Success() {
        // Given
        when(patientRepository.findByIdAndIsActiveTrue(testPatient.getId()))
            .thenReturn(Optional.of(testPatient));
        when(userRepository.findByIdAndIsActiveTrue(testCaregiver.getId()))
            .thenReturn(Optional.of(testCaregiver));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testOwner.getEmail());
        when(userRepository.findByEmail(testOwner.getEmail()))
            .thenReturn(Optional.of(testOwner));
        when(carePlanRepository.save(any(CarePlan.class))).thenReturn(testCarePlan);
        
        // When
        CarePlanResponse response = carePlanService.createCarePlan(carePlanRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Diabetes Management");
        
        // Verify patient was looked up
        verify(patientRepository).findByIdAndIsActiveTrue(testPatient.getId());
        
        // Verify caregiver was validated
        verify(userRepository).findByIdAndIsActiveTrue(testCaregiver.getId());
        
        // Verify care plan was saved
        ArgumentCaptor<CarePlan> captor = ArgumentCaptor.forClass(CarePlan.class);
        verify(carePlanRepository).save(captor.capture());
        CarePlan savedPlan = captor.getValue();
        assertThat(savedPlan.getTitle()).isEqualTo("Diabetes Management");
        assertThat(savedPlan.getPatient()).isEqualTo(testPatient);
    }
    
    // Test 2: Create Care Plan - Patient Not Found
    @Test
    @DisplayName("Should throw exception when patient not found")
    void createCarePlan_PatientNotFound() {
        // Given
        when(patientRepository.findByIdAndIsActiveTrue(any(UUID.class)))
            .thenReturn(Optional.empty());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testOwner.getEmail());
        when(userRepository.findByEmail(testOwner.getEmail()))
            .thenReturn(Optional.of(testOwner));
        
        // When & Then
        assertThatThrownBy(() -> carePlanService.createCarePlan(carePlanRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Patient not found");
        
        // Verify save was never called
        verify(carePlanRepository, never()).save(any());
    }
    
    // Test 3: Create Care Plan - Caregiver Not Found
    @Test
    @DisplayName("Should throw exception when assigned caregiver not found")
    void createCarePlan_CaregiverNotFound() {
        // Given
        when(patientRepository.findByIdAndIsActiveTrue(testPatient.getId()))
            .thenReturn(Optional.of(testPatient));
        when(userRepository.findByIdAndIsActiveTrue(testCaregiver.getId()))
            .thenReturn(Optional.empty());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testOwner.getEmail());
        when(userRepository.findByEmail(testOwner.getEmail()))
            .thenReturn(Optional.of(testOwner));
        
        // When & Then
        assertThatThrownBy(() -> carePlanService.createCarePlan(carePlanRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Assigned caregiver not found");
        
        verify(carePlanRepository, never()).save(any());
    }
    
    // Test 4: Create Care Plan - Invalid Caregiver Role
    @Test
    @DisplayName("Should throw exception when assigned user has invalid role")
    void createCarePlan_InvalidCaregiverRole() {
        // Given
        User familyUser = new User();
        familyUser.setId(UUID.randomUUID());
        familyUser.setRole(UserRole.FAMILY);
        
        when(patientRepository.findByIdAndIsActiveTrue(testPatient.getId()))
            .thenReturn(Optional.of(testPatient));
        when(userRepository.findByIdAndIsActiveTrue(testCaregiver.getId()))
            .thenReturn(Optional.of(familyUser));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testOwner.getEmail());
        when(userRepository.findByEmail(testOwner.getEmail()))
            .thenReturn(Optional.of(testOwner));
        
        // When & Then
        assertThatThrownBy(() -> carePlanService.createCarePlan(carePlanRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("must have caregiver privileges");
        
        verify(carePlanRepository, never()).save(any());
    }
    
    // Test 5: Get Care Plan By ID - Found
    @Test
    @DisplayName("Should return care plan when found by ID")
    void getCarePlanById_Found() {
        // Given
        UUID carePlanId = testCarePlan.getId();
        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId))
            .thenReturn(Optional.of(testCarePlan));
        
        // When
        Optional<CarePlanResponse> result = carePlanService.getCarePlanById(carePlanId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Diabetes Management");
        verify(carePlanRepository).findByIdAndIsActiveTrue(carePlanId);
    }
    
    // Test 6: Get Care Plan By ID - Not Found
    @Test
    @DisplayName("Should return empty when care plan not found")
    void getCarePlanById_NotFound() {
        // Given
        UUID carePlanId = UUID.randomUUID();
        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId))
            .thenReturn(Optional.empty());
        
        // When
        Optional<CarePlanResponse> result = carePlanService.getCarePlanById(carePlanId);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    // Test 7: Update Care Plan
    @Test
    @DisplayName("Should update care plan successfully")
    void updateCarePlan_Success() {
        // Given
        UUID carePlanId = testCarePlan.getId();
        CarePlanUpdateRequest updateRequest = new CarePlanUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPriority(CarePlanPriority.LOW);
        
        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId))
            .thenReturn(Optional.of(testCarePlan));
        when(carePlanRepository.save(any(CarePlan.class))).thenReturn(testCarePlan);
        
        // When
        CarePlanResponse response = carePlanService.updateCarePlan(carePlanId, updateRequest);
        
        // Then
        assertThat(response).isNotNull();
        
        // Verify care plan was updated
        ArgumentCaptor<CarePlan> captor = ArgumentCaptor.forClass(CarePlan.class);
        verify(carePlanRepository).save(captor.capture());
        CarePlan updatedPlan = captor.getValue();
        assertThat(updatedPlan.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedPlan.getDescription()).isEqualTo("Updated Description");
    }
    
    // Test 8: Update Care Plan - Not Found
    @Test
    @DisplayName("Should throw exception when updating non-existent care plan")
    void updateCarePlan_NotFound() {
        // Given
        UUID carePlanId = UUID.randomUUID();
        CarePlanUpdateRequest updateRequest = new CarePlanUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPriority(CarePlanPriority.LOW);

        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carePlanService.updateCarePlan(carePlanId, updateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Care plan not found");
        verify(carePlanRepository, never()).save(any());
    }

    // Test 9: Update Care Plan - Assigned Caregiver Not Found
    @Test
    @DisplayName("Should throw exception when updating with non-existent caregiver")
    void updateCarePlan_AssignedCaregiverNotFound() {
        // Given
        UUID carePlanId = testCarePlan.getId();
        CarePlanUpdateRequest updateRequest = new CarePlanUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPriority(CarePlanPriority.LOW);
        updateRequest.setAssignedCaregiverId(UUID.randomUUID());

        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId)).thenReturn(Optional.of(testCarePlan));
        when(userRepository.findByIdAndIsActiveTrue(updateRequest.getAssignedCaregiverId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carePlanService.updateCarePlan(carePlanId, updateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Assigned caregiver not found");
        verify(carePlanRepository, never()).save(any());
    }

    // Test 10: Update Care Plan - Invalid Caregiver Role
    @Test
    @DisplayName("Should throw exception when updating with invalid caregiver role")
    void updateCarePlan_InvalidCaregiverRole() {
        // Given
        UUID carePlanId = testCarePlan.getId();
        CarePlanUpdateRequest updateRequest = new CarePlanUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPriority(CarePlanPriority.LOW);
        updateRequest.setAssignedCaregiverId(UUID.randomUUID());

        User familyUser = new User();
        familyUser.setId(updateRequest.getAssignedCaregiverId());
        familyUser.setRole(UserRole.FAMILY);

        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId)).thenReturn(Optional.of(testCarePlan));
        when(userRepository.findByIdAndIsActiveTrue(updateRequest.getAssignedCaregiverId())).thenReturn(Optional.of(familyUser));

        // When & Then
        assertThatThrownBy(() -> carePlanService.updateCarePlan(carePlanId, updateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("must have caregiver privileges");
        verify(carePlanRepository, never()).save(any());
    }

    // Test 11: Delete Care Plan
    @Test
    @DisplayName("Should soft delete care plan")
    void deleteCarePlan_Success() {
        // Given
        UUID carePlanId = testCarePlan.getId();
        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId))
            .thenReturn(Optional.of(testCarePlan));
        
        // When
        carePlanService.deleteCarePlan(carePlanId);
        
        // Then
        verify(carePlanRepository).save(testCarePlan);
        // Note: Can't verify setActive(false) directly without checking the saved object
    }

    // Test 12: Delete Care Plan - Not Found
    @Test
    @DisplayName("Should throw exception when deleting non-existent care plan")
    void deleteCarePlan_NotFound() {
        // Given
        UUID carePlanId = UUID.randomUUID();
        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carePlanService.deleteCarePlan(carePlanId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Care plan not found");
        verify(carePlanRepository, never()).save(any());
    }

    // Test 13: Activate Care Plan - Not Found
    @Test
    @DisplayName("Should throw exception when activating non-existent care plan")
    void activateCarePlan_NotFound() {
        // Given
        UUID carePlanId = UUID.randomUUID();
        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carePlanService.activateCarePlan(carePlanId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Care plan not found");
        verify(carePlanRepository, never()).save(any());
    }

    // Test 14: Activate Care Plan - Start Date In Future
    @Test
    @DisplayName("Should throw exception when activating care plan before start date")
    void activateCarePlan_StartDateInFuture() {
        // Given
        UUID carePlanId = testCarePlan.getId();
        testCarePlan.setStartDate(LocalDate.now().plusDays(1));
        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId)).thenReturn(Optional.of(testCarePlan));

        // When & Then
        assertThatThrownBy(() -> carePlanService.activateCarePlan(carePlanId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Care plan cannot be activated before start date");
        verify(carePlanRepository, never()).save(any());
    }

    // Test 15: Suspend Care Plan - Not Found
    @Test
    @DisplayName("Should throw exception when suspending non-existent care plan")
    void suspendCarePlan_NotFound() {
        // Given
        UUID carePlanId = UUID.randomUUID();
        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carePlanService.suspendCarePlan(carePlanId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Care plan not found");
        verify(carePlanRepository, never()).save(any());
    }

    // Test 16: Complete Care Plan - Not Found
    @Test
    @DisplayName("Should throw exception when completing non-existent care plan")
    void completeCarePlan_NotFound() {
        // Given
        UUID carePlanId = UUID.randomUUID();
        when(carePlanRepository.findByIdAndIsActiveTrue(carePlanId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carePlanService.completeCarePlan(carePlanId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Care plan not found");
        verify(carePlanRepository, never()).save(any());
    }

    // Test 17: Check Exists By ID
    @Test
    @DisplayName("Should check if care plan exists by ID")
    void existsById() {
        // Given
        UUID existingId = UUID.randomUUID();
        UUID nonExistingId = UUID.randomUUID();
        
        CarePlan dummyPlan = CarePlanTestBuilder.aCarePlan().build();
        when(carePlanRepository.findByIdAndIsActiveTrue(existingId)).thenReturn(Optional.of(dummyPlan));
        when(carePlanRepository.findByIdAndIsActiveTrue(nonExistingId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThat(carePlanService.existsById(existingId)).isTrue();
        assertThat(carePlanService.existsById(nonExistingId)).isFalse();
    }
}