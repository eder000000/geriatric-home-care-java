package com.geriatriccare.controller.ai;

import com.geriatriccare.dto.ai.InteractionCheckRequest;
import com.geriatriccare.dto.ai.InteractionCheckResponse;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.service.ai.MedicationInteractionService;
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
@RequestMapping("/api/ai/interactions")
public class MedicationInteractionController {
    
    private static final Logger log = LoggerFactory.getLogger(MedicationInteractionController.class);
    
    private final MedicationInteractionService interactionService;
    
    @Autowired
    public MedicationInteractionController(MedicationInteractionService interactionService) {
        this.interactionService = interactionService;
    }
    
    @PostMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<InteractionCheckResponse> checkInteractions(
            @Valid @RequestBody InteractionCheckRequest request) {
        
        log.info("Interaction check request for patient: {}", request.getPatientId());
        
        try {
            InteractionCheckResponse response = interactionService.checkInteractions(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to check interactions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<List<AIAuditLog>> getInteractionHistory(@PathVariable UUID patientId) {
        
        log.info("Retrieving interaction history for patient: {}", patientId);
        
        try {
            List<AIAuditLog> history = interactionService.getInteractionHistory(patientId);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            log.error("Failed to retrieve history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Medication Interaction Service is running");
    }
}
