package com.geriatriccare.controller.ai;

import com.geriatriccare.dto.ai.SymptomAnalysisRequest;
import com.geriatriccare.dto.ai.SymptomAnalysisResponse;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.service.ai.SymptomAnalysisService;
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
@RequestMapping("/api/ai/symptoms")
public class SymptomAnalysisController {
    
    private static final Logger log = LoggerFactory.getLogger(SymptomAnalysisController.class);
    
    private final SymptomAnalysisService symptomService;
    
    @Autowired
    public SymptomAnalysisController(SymptomAnalysisService symptomService) {
        this.symptomService = symptomService;
    }
    
    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<SymptomAnalysisResponse> analyzeSymptoms(
            @Valid @RequestBody SymptomAnalysisRequest request) {
        
        log.info("Symptom analysis request for patient: {}", request.getPatientId());
        
        try {
            SymptomAnalysisResponse response = symptomService.analyzeSymptoms(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to analyze symptoms: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<List<AIAuditLog>> getSymptomHistory(@PathVariable UUID patientId) {
        
        log.info("Retrieving symptom analysis history for patient: {}", patientId);
        
        try {
            List<AIAuditLog> history = symptomService.getSymptomAnalysisHistory(patientId);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            log.error("Failed to retrieve history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Symptom Analysis Service is running");
    }
}
