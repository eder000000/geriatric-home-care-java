package com.geriatriccare.service;

import com.geriatriccare.dto.CarePlanRequest;
import com.geriatriccare.dto.CarePlanResponse;
import com.geriatriccare.dto.CarePlanUpdateRequest;
import com.geriatriccare.dto.summary.CaregiverSummary;
import com.geriatriccare.entity.*;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.repository.CarePlanRepository;
import com.geriatriccare.repository.PatientRepository;
import com.geriatriccare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarePlanService {
    
    private static final Logger logger = LoggerFactory.getLogger(CarePlanService.class);
    
    @Autowired
    private CarePlanRepository carePlanRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Create a new care plan
     */
    public CarePlanResponse createCarePlan(CarePlanRequest request) {
        logger.info("Creating care plan: {} for patient: {}", request.getTitle(), request.getPatientId());
        
        // Validate patient exists
        Patient patient = patientRepository.findByIdAndIsActiveTrue(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        // Get current user as creator
        User createdBy = getCurrentUser();
        
        // Validate assigned caregiver if provided
        User assignedCaregiver = null;
        if (request.getAssignedCaregiverId() != null) {
            assignedCaregiver = userRepository.findByIdAndIsActiveTrue(request.getAssignedCaregiverId())
                    .orElseThrow(() -> new RuntimeException("Assigned caregiver not found"));
            
            // Validate caregiver role
            if (assignedCaregiver.getRole() != UserRole.CAREGIVER && 
                assignedCaregiver.getRole() != UserRole.ADMIN &&
                assignedCaregiver.getRole() != UserRole.ADMIN) {
                throw new RuntimeException("Assigned user must have caregiver privileges");
            }
        }
        
        // Create care plan
        CarePlan carePlan = new CarePlan();
        carePlan.setPatient(patient);
        carePlan.setTitle(request.getTitle());
        carePlan.setDescription(request.getDescription());
        carePlan.setPriority(request.getPriority());
        carePlan.setStartDate(request.getStartDate());
        carePlan.setEndDate(request.getEndDate());
        carePlan.setCreatedBy(createdBy);
        carePlan.setAssignedCaregiver(assignedCaregiver);
        carePlan.setStatus(CarePlanStatus.DRAFT);
        
        CarePlan saved = carePlanRepository.save(carePlan);
        logger.info("Successfully created care plan with ID: {}", saved.getId());
        
        return convertToResponse(saved);
    }
    
    /**
     * Get care plan by ID
     */
    @Transactional(readOnly = true)
    public Optional<CarePlanResponse> getCarePlanById(UUID id) {
        logger.debug("Fetching care plan by ID: {}", id);
        
        return carePlanRepository.findByIdAndIsActiveTrue(id)
                .map(this::convertToResponse);
    }
    
    /**
     * Get all care plans with pagination and filters
     */
    @Transactional(readOnly = true)
    public Page<CarePlanResponse> getAllCarePlans(int page, int size, String sortBy, String sortDir, 
                                                  UUID patientId, CarePlanStatus status) {
        logger.debug("Fetching care plans - page: {}, size: {}, patient: {}, status: {}", 
                    page, size, patientId, status);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CarePlan> carePlans;
        
        if (patientId != null && status != null) {
            carePlans = carePlanRepository.findByPatientIdAndStatusAndIsActiveTrue(patientId, status, pageable);
        } else if (patientId != null) {
            carePlans = carePlanRepository.findByPatientIdAndIsActiveTrue(patientId, pageable);
        } else if (status != null) {
            carePlans = carePlanRepository.findByStatusAndIsActiveTrue(status, pageable);
        } else {
            carePlans = carePlanRepository.findByIsActiveTrue(pageable);
        }
        
        return carePlans.map(this::convertToResponse);
    }
    
    /**
     * Update care plan
     */
    public CarePlanResponse updateCarePlan(UUID id, CarePlanUpdateRequest request) {
        logger.info("Updating care plan: {}", id);
        
        CarePlan carePlan = carePlanRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Care plan not found"));
        
        // Validate assigned caregiver if provided
        if (request.getAssignedCaregiverId() != null) {
            User assignedCaregiver = userRepository.findByIdAndIsActiveTrue(request.getAssignedCaregiverId())
                    .orElseThrow(() -> new RuntimeException("Assigned caregiver not found"));
            
            if (assignedCaregiver.getRole() != UserRole.CAREGIVER && 
                assignedCaregiver.getRole() != UserRole.ADMIN &&
                assignedCaregiver.getRole() != UserRole.ADMIN) {
                throw new RuntimeException("Assigned user must have caregiver privileges");
            }
            
            carePlan.setAssignedCaregiver(assignedCaregiver);
        }
        
        // Update fields
        carePlan.setTitle(request.getTitle());
        carePlan.setDescription(request.getDescription());
        carePlan.setPriority(request.getPriority());
        carePlan.setStartDate(request.getStartDate());
        carePlan.setEndDate(request.getEndDate());
        
        CarePlan updated = carePlanRepository.save(carePlan);
        logger.info("Successfully updated care plan: {}", id);
        
        return convertToResponse(updated);
    }
    
    /**
     * Soft delete care plan
     */
    public void deleteCarePlan(UUID id) {
        logger.info("Soft deleting care plan: {}", id);
        
        CarePlan carePlan = carePlanRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Care plan not found"));
        
        carePlan.setIsActive(false);
        carePlanRepository.save(carePlan);
        
        logger.info("Successfully soft deleted care plan: {}", id);
    }
    
    // ========== STATUS MANAGEMENT ==========
    
    /**
     * Activate care plan
     */
    public void activateCarePlan(UUID id) {
        logger.info("Activating care plan: {}", id);
        
        CarePlan carePlan = carePlanRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Care plan not found"));
        
        // Validate care plan can be activated
        if (carePlan.getStartDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("Care plan cannot be activated before start date");
        }
        
        carePlan.setStatus(CarePlanStatus.ACTIVE);
        carePlanRepository.save(carePlan);
        
        logger.info("Successfully activated care plan: {}", id);
    }
    
    /**
     * Suspend care plan
     */
    public void suspendCarePlan(UUID id) {
        logger.info("Suspending care plan: {}", id);
        
        CarePlan carePlan = carePlanRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Care plan not found"));
        
        carePlan.setStatus(CarePlanStatus.SUSPENDED);
        carePlanRepository.save(carePlan);
        
        logger.info("Successfully suspended care plan: {}", id);
    }
    
    /**
     * Complete care plan
     */
    public void completeCarePlan(UUID id) {
        logger.info("Completing care plan: {}", id);
        
        CarePlan carePlan = carePlanRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Care plan not found"));
        
        carePlan.setStatus(CarePlanStatus.COMPLETED);
        carePlanRepository.save(carePlan);
        
        logger.info("Successfully completed care plan: {}", id);
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String identifier = auth.getName();

        // identifier may be email (register token) or username (login token)
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
    
    /**
     * Check if care plan exists and is active
     */
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return carePlanRepository.findByIdAndIsActiveTrue(id).isPresent();
    }
    
    // ========== CONVERSION METHODS ==========
    
    /**
     * Convert CarePlan entity to CarePlanResponse DTO
     */
    private CarePlanResponse convertToResponse(CarePlan carePlan) {
        CarePlanResponse response = new CarePlanResponse();
        
        // Basic care plan information
        response.setId(carePlan.getId());
        response.setTitle(carePlan.getTitle());
        response.setDescription(carePlan.getDescription());
        response.setPriority(carePlan.getPriority());
        response.setStatus(carePlan.getStatus());
        response.setStartDate(carePlan.getStartDate());
        response.setEndDate(carePlan.getEndDate());
        response.setIsActive(carePlan.getIsActive());
        response.setCreatedAt(carePlan.getCreatedAt());
        response.setUpdatedAt(carePlan.getUpdatedAt());
        
        // Patient information
        response.setPatientId(carePlan.getPatient().getId());
        response.setPatientName(carePlan.getPatient().getFullName());
        
        // Caregiver information
        if (carePlan.getAssignedCaregiver() != null) {
            response.setAssignedCaregiver(new CaregiverSummary(
                    carePlan.getAssignedCaregiver().getId(),
                    carePlan.getAssignedCaregiver().getFirstName(),
                    carePlan.getAssignedCaregiver().getLastName(),
                    carePlan.getAssignedCaregiver().getEmail(),
                    false // Not primary caregiver context here
            ));
        }
        
        // Care task summaries (will be implemented when we add task management)
        response.setCreatedBy(new CaregiverSummary(
            carePlan.getCreatedBy().getId(),     // or whatever the actual method is
            carePlan.getCreatedBy().getFirstName(),
            carePlan.getCreatedBy().getLastName(),
            carePlan.getCreatedBy().getEmail(),
            false
        ));
        return response;
    }
}