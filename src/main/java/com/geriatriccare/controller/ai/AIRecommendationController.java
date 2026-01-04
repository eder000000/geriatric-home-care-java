package com.geriatriccare.controller.ai;

import com.geriatriccare.dto.ai.RecommendationRequest;
import com.geriatriccare.dto.ai.RecommendationResponse;
import com.geriatriccare.dto.ai.RecommendationType;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.service.ai.AIRecommendationService;
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
@RequestMapping("/api/ai/recommendations")
public class AIRecommendationController {
    
    private static final Logger log = LoggerFactory.getLogger(AIRecommendationController.class);
    
    private final AIRecommendationService recommendationService;
    
    @Autowired
    public AIRecommendationController(AIRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    
    /**
     * Generate AI-powered recommendation
     * POST /api/ai/recommendations
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<RecommendationResponse> generateRecommendation(
            @Valid @RequestBody RecommendationRequest request) {
        
        log.info("Received recommendation request for patient: {}, type: {}", 
                 request.getPatientId(), request.getType());
        
        try {
            RecommendationResponse response = recommendationService.generateRecommendation(request);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Failed to generate recommendation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get recommendation history for a patient
     * GET /api/ai/recommendations/history/{patientId}
     */
    @GetMapping("/history/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<List<AIAuditLog>> getPatientHistory(
            @PathVariable UUID patientId) {
        
        log.info("Retrieving recommendation history for patient: {}", patientId);
        
        try {
            List<AIAuditLog> history = recommendationService.getPatientRecommendationHistory(patientId);
            return ResponseEntity.ok(history);
            
        } catch (RuntimeException e) {
            log.error("Failed to retrieve history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Get recommendation history by type for a patient
     * GET /api/ai/recommendations/history/{patientId}/type/{type}
     */
    @GetMapping("/history/{patientId}/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<List<AIAuditLog>> getPatientHistoryByType(
            @PathVariable UUID patientId,
            @PathVariable RecommendationType type) {
        
        log.info("Retrieving {} recommendations for patient: {}", type, patientId);
        
        try {
            List<AIAuditLog> history = recommendationService.getPatientRecommendationsByType(patientId, type);
            return ResponseEntity.ok(history);
            
        } catch (RuntimeException e) {
            log.error("Failed to retrieve history by type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Get recent recommendations across all patients (Admin only)
     * GET /api/ai/recommendations/recent?limit=10
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AIAuditLog>> getRecentRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Retrieving {} most recent recommendations", limit);
        
        if (limit < 1 || limit > 100) {
            log.warn("Invalid limit: {}. Using default 10", limit);
            limit = 10;
        }
        
        try {
            List<AIAuditLog> recent = recommendationService.getRecentRecommendations(limit);
            return ResponseEntity.ok(recent);
            
        } catch (Exception e) {
            log.error("Failed to retrieve recent recommendations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/ai/recommendations/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Recommendation Service is running");
    }
}
