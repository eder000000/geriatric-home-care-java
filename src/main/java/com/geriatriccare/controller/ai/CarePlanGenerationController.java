package com.geriatriccare.controller.ai;

import com.geriatriccare.dto.ai.CarePlanGenerationRequest;
import com.geriatriccare.dto.ai.CarePlanGenerationResponse;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.service.ai.CarePlanGenerationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai/care-plans")
public class CarePlanGenerationController {
    
    private static final Logger log = LoggerFactory.getLogger(CarePlanGenerationController.class);
    
    private final CarePlanGenerationService carePlanService;
    
    @Autowired
    public CarePlanGenerationController(CarePlanGenerationService carePlanService) {
        this.carePlanService = carePlanService;
    }
    
    /**
     * Generate AI-powered care plan
     * POST /api/ai/care-plans
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<CarePlanGenerationResponse> generateCarePlan(
            @Valid @RequestBody CarePlanGenerationRequest request) {
        
        log.info("Received care plan generation request for patient: {}", request.getPatientId());
        
        try {
            CarePlanGenerationResponse response = carePlanService.generateCarePlan(request);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Failed to generate care plan: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Regenerate care plan with updated information
     * PUT /api/ai/care-plans/patient/{patientId}
     */
    @PutMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<CarePlanGenerationResponse> regenerateCarePlan(
            @PathVariable UUID patientId,
            @Valid @RequestBody CarePlanGenerationRequest request) {
        
        log.info("Regenerating care plan for patient: {}", patientId);
        
        try {
            CarePlanGenerationResponse response = carePlanService.regenerateCarePlan(patientId, request);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Failed to regenerate care plan: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Get care plan generation history for a patient
     * GET /api/ai/care-plans/patient/{patientId}/history
     */
    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<List<AIAuditLog>> getCarePlanHistory(@PathVariable UUID patientId) {
        
        log.info("Retrieving care plan history for patient: {}", patientId);
        
        try {
            List<AIAuditLog> history = carePlanService.getCarePlanHistory(patientId);
            return ResponseEntity.ok(history);
            
        } catch (RuntimeException e) {
            log.error("Failed to retrieve care plan history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/ai/care-plans/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Care Plan Generation Service is running");
    }
}
