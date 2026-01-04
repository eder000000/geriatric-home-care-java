package com.geriatriccare.service.ai;

import com.geriatriccare.dto.ai.RecommendationRequest;
import com.geriatriccare.dto.ai.RecommendationResponse;
import com.geriatriccare.dto.ai.RecommendationType;
import com.geriatriccare.dto.ai.PromptTemplateResponse;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.repository.AIAuditLogRepository;
import com.geriatriccare.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AIRecommendationService {
    
    private static final Logger log = LoggerFactory.getLogger(AIRecommendationService.class);
    
    private final OpenAIService openAIService;
    private final PromptTemplateService promptTemplateService;
    private final AIAuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    
    @Autowired
    public AIRecommendationService(
            OpenAIService openAIService,
            PromptTemplateService promptTemplateService,
            AIAuditLogRepository auditLogRepository,
            PatientRepository patientRepository) {
        this.openAIService = openAIService;
        this.promptTemplateService = promptTemplateService;
        this.auditLogRepository = auditLogRepository;
        this.patientRepository = patientRepository;
    }
    
    /**
     * Generate AI-powered recommendation for a patient
     */
    @Transactional
    public RecommendationResponse generateRecommendation(RecommendationRequest request) {
        log.info("Generating {} recommendation for patient: {}", 
                 request.getType(), request.getPatientId());
        
        // 1. Validate patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found: " + request.getPatientId()));
        
        // 2. Get prompt template
        PromptTemplateResponse template = promptTemplateService
                .getLatestTemplateByName(request.getTemplateName());
        
        // 3. Enrich context with patient data
        Map<String, String> enrichedContext = enrichContextWithPatientData(
                request.getContextVariables(), patient);
        
        // 4. Render the template
        String renderedPrompt = promptTemplateService.renderTemplate(
                template.getTemplate(), enrichedContext);
        
        // 5. Add additional notes if provided
        if (request.getAdditionalNotes() != null && !request.getAdditionalNotes().isEmpty()) {
            renderedPrompt += "\n\nAdditional Notes:\n" + request.getAdditionalNotes();
        }
        
        log.debug("Rendered prompt for OpenAI (length: {} chars)", renderedPrompt.length());
        
        // 6. Call OpenAI API
        String aiResponse;
        try {
            aiResponse = openAIService.generateCompletion(renderedPrompt, template.getName())
                    .getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate AI recommendation: " + e.getMessage(), e);
        }
        
        // 7. Create audit log
        UUID auditLogId = null;
        if (request.getSaveToHistory()) {
            AIAuditLog auditLog = createAuditLog(
                    request.getPatientId(),
                    request.getType().name(),
                    renderedPrompt,
                    aiResponse
            );
            auditLogId = auditLog.getId();
        }
        
        // 8. Build response
        RecommendationResponse response = new RecommendationResponse();
        response.setId(auditLogId);
        response.setPatientId(request.getPatientId());
        response.setType(request.getType());
        response.setTemplateUsed(template.getName());
        response.setRecommendation(aiResponse);
        response.setPrompt(renderedPrompt);
        response.setModel("gpt-4");
        response.setGeneratedAt(LocalDateTime.now());
        response.setGeneratedBy(getCurrentUserId());
        response.setSavedToHistory(request.getSaveToHistory());
        
        log.info("Successfully generated recommendation (ID: {}) for patient: {}", 
                 auditLogId, request.getPatientId());
        
        return response;
    }
    
    /**
     * Get recommendation history for a patient
     */
    @Transactional(readOnly = true)
    public List<AIAuditLog> getPatientRecommendationHistory(UUID patientId) {
        log.info("Retrieving recommendation history for patient: {}", patientId);
        
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found: " + patientId);
        }
        
        return auditLogRepository.findByPatientIdOrderByTimestampDesc(patientId);
    }
    
    /**
     * Get recommendation history by type
     */
    @Transactional(readOnly = true)
    public List<AIAuditLog> getPatientRecommendationsByType(
            UUID patientId, RecommendationType type) {
        
        log.info("Retrieving {} history for patient: {}", type, patientId);
        
        return auditLogRepository.findByPatientIdAndRequestTypeOrderByTimestampDesc(
                patientId, type.name());
    }
    
    /**
     * Get recent recommendations across all patients (admin view)
     */
    @Transactional(readOnly = true)
    public List<AIAuditLog> getRecentRecommendations(int limit) {
        log.info("Retrieving {} most recent recommendations", limit);
        
        return auditLogRepository.findTop10ByOrderByTimestampDesc()
                .stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // ==================== Private Helper Methods ====================
    
    /**
     * Enrich context variables with patient demographic data
     */
    private Map<String, String> enrichContextWithPatientData(
            Map<String, String> contextVariables, Patient patient) {
        
        Map<String, String> enriched = new HashMap<>(contextVariables);
        
        // Add patient name if not present
        if (!enriched.containsKey("patientName")) {
            enriched.put("patientName", patient.getFirstName() + " " + patient.getLastName());
        }
        
        // Add age if not present
        if (!enriched.containsKey("age") && patient.getDateOfBirth() != null) {
            int age = java.time.Period.between(
                    patient.getDateOfBirth(), 
                    java.time.LocalDate.now()
            ).getYears();
            enriched.put("age", String.valueOf(age));
        }
        
        // Add patient ID
        if (!enriched.containsKey("patientId")) {
            enriched.put("patientId", patient.getId().toString());
        }
        
        log.debug("Enriched context with patient data. Variables: {}", enriched.keySet());
        
        return enriched;
    }
    
    /**
     * Create audit log entry
     */
    private AIAuditLog createAuditLog(
            UUID patientId,
            String requestType,
            String prompt,
            String response) {
        
        AIAuditLog auditLog = new AIAuditLog();
        auditLog.setPatientId(patientId);
        auditLog.setUserId(getCurrentUserId());
        auditLog.setRequestType(requestType);
        auditLog.setPrompt(prompt);
        auditLog.setResponse(response);
        auditLog.setSuccess(true);
        auditLog.setTimestamp(LocalDateTime.now());
        
        AIAuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Created audit log entry: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * Get current authenticated user ID
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            
            String email = userDetails.getUsername();
            log.debug("Current user: {}", email);
            
            // Placeholder - in production lookup user by email
            return UUID.randomUUID();
        }
        
        log.warn("No authenticated user found, using system UUID");
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}