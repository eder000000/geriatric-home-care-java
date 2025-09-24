package com.geriatriccare.controller;

import com.geriatriccare.dto.CarePlanRequest;
import com.geriatriccare.dto.CarePlanResponse;
import com.geriatriccare.dto.CarePlanUpdateRequest;
import com.geriatriccare.entity.CarePlanStatus;
import com.geriatriccare.service.CarePlanService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/care-plans")
@CrossOrigin(origins = "*")
public class CarePlanController {
    
    private static final Logger logger = LoggerFactory.getLogger(CarePlanController.class);
    
    @Autowired
    private CarePlanService carePlanService;
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * Create a new care plan
     * Only OWNER, ADMIN, MANAGER, and CAREGIVER can create care plans
     */
    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER')")
    public ResponseEntity<CarePlanResponse> createCarePlan(@Valid @RequestBody CarePlanRequest request) {
        logger.info("Creating care plan: {} for patient: {}", request.getTitle(), request.getPatientId());
        
        try {
            CarePlanResponse response = carePlanService.createCarePlan(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating care plan", e);
            throw new RuntimeException("Failed to create care plan: " + e.getMessage());
        }
    }
    
    /**
     * Get care plan by ID
     * All authenticated users can view care plans (with proper filtering by service)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<CarePlanResponse> getCarePlan(@PathVariable UUID id) {
        logger.info("Fetching care plan: {}", id);
        
        return carePlanService.getCarePlanById(id)
                .map(carePlan -> ResponseEntity.ok(carePlan))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all care plans with pagination and filtering
     * OWNER, ADMIN, MANAGER can see all; CAREGIVER sees assigned; FAMILY sees patient's
     */
    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<Page<CarePlanResponse>> getAllCarePlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) CarePlanStatus status) {
        
        logger.info("Fetching care plans - page: {}, size: {}, patient: {}, status: {}", 
                   page, size, patientId, status);
        
        Page<CarePlanResponse> carePlans = carePlanService.getAllCarePlans(page, size, sortBy, sortDir, patientId, status);
        return ResponseEntity.ok(carePlans);
    }
    
    /**
     * Update care plan
     * Only OWNER, ADMIN, MANAGER, and assigned CAREGIVER can update
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER')")
    public ResponseEntity<CarePlanResponse> updateCarePlan(
            @PathVariable UUID id, 
            @Valid @RequestBody CarePlanUpdateRequest request) {
        
        logger.info("Updating care plan: {}", id);
        
        try {
            CarePlanResponse response = carePlanService.updateCarePlan(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error updating care plan: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Soft delete care plan
     * Only OWNER and ADMIN can delete care plans
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCarePlan(@PathVariable UUID id) {
        logger.info("Deleting care plan: {}", id);
        
        try {
            carePlanService.deleteCarePlan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting care plan: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== STATUS MANAGEMENT ==========
    
    /**
     * Activate care plan
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER')")
    public ResponseEntity<Void> activateCarePlan(@PathVariable UUID id) {
        logger.info("Activating care plan: {}", id);
        
        try {
            carePlanService.activateCarePlan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error activating care plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Suspend care plan
     */
    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER')")
    public ResponseEntity<Void> suspendCarePlan(@PathVariable UUID id) {
        logger.info("Suspending care plan: {}", id);
        
        try {
            carePlanService.suspendCarePlan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error suspending care plan: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Complete care plan
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER')")
    public ResponseEntity<Void> completeCarePlan(@PathVariable UUID id) {
        logger.info("Completing care plan: {}", id);
        
        try {
            carePlanService.completeCarePlan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error completing care plan: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== UTILITY ENDPOINTS ==========
    
    /**
     * Check if care plan exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER')")
    public ResponseEntity<Boolean> carePlanExists(@PathVariable UUID id) {
        logger.info("Checking if care plan exists: {}", id);
        
        boolean exists = carePlanService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}