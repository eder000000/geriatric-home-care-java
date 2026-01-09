package com.geriatriccare.service.ai;

import com.geriatriccare.dto.ai.*;
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
import java.util.stream.Collectors;

@Service
public class CarePlanGenerationService {
    
    private static final Logger log = LoggerFactory.getLogger(CarePlanGenerationService.class);
    
    private final OpenAIService openAIService;
    private final PromptTemplateService promptTemplateService;
    private final AIAuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    
    @Autowired
    public CarePlanGenerationService(
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
     * Generate comprehensive AI-powered care plan
     */
    @Transactional
    public CarePlanGenerationResponse generateCarePlan(CarePlanGenerationRequest request) {
        log.info("Generating care plan for patient: {} with focus areas: {}", 
                 request.getPatientId(), request.getFocusAreas());
        
        // 1. Validate patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found: " + request.getPatientId()));
        
        // 2. Get prompt template
        PromptTemplateResponse template = promptTemplateService
                .getLatestTemplateByName("Personalized Care Plan Generator");
        
        // 3. Build comprehensive context
        Map<String, String> context = buildCarePlanContext(request, patient);
        
        // 4. Render the prompt
        String renderedPrompt = promptTemplateService.renderTemplate(
                template.getTemplate(), context);
        
        log.debug("Rendered care plan prompt (length: {} chars)", renderedPrompt.length());
        
        // 5. Call OpenAI API
        String aiResponse;
        try {
            OpenAIResponse openAIResponse = openAIService.generateCompletion(
                    renderedPrompt, "Care Plan Generation");
            aiResponse = openAIResponse.getChoices().get(0).getMessage().getContent();
            
        } catch (Exception e) {
            log.error("OpenAI API call failed for care plan generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate care plan: " + e.getMessage(), e);
        }
        
        // 6. Parse AI response into structured sections
        CarePlanGenerationResponse response = parseCarePlanResponse(aiResponse, request, patient);
        
        // 7. Create audit log
        if (request.getSaveToDatabase()) {
            AIAuditLog auditLog = createAuditLog(
                    request.getPatientId(),
                    "CARE_PLAN_GENERATION",
                    renderedPrompt,
                    aiResponse
            );
            response.setId(auditLog.getId());
        }
        
        // 8. Set metadata
        response.setPatientId(patient.getId());
        response.setPatientName(patient.getFirstName() + " " + patient.getLastName());
        response.setFocusAreas(request.getFocusAreas());
        response.setTimeframe(request.getTimeframe());
        response.setAiModel("gpt-4");
        response.setTemplateUsed(template.getName());
        response.setPrompt(renderedPrompt);
        response.setGeneratedAt(LocalDateTime.now());
        response.setGeneratedBy(getCurrentUserId());
        response.setSavedToDatabase(request.getSaveToDatabase());
        
        log.info("Successfully generated care plan for patient: {}", request.getPatientId());
        
        return response;
    }
    
    /**
     * Get care plan generation history for a patient
     */
    @Transactional(readOnly = true)
    public List<AIAuditLog> getCarePlanHistory(UUID patientId) {
        log.info("Retrieving care plan history for patient: {}", patientId);
        
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found: " + patientId);
        }
        
        return auditLogRepository.findByPatientIdAndRequestTypeOrderByTimestampDesc(
                patientId, "CARE_PLAN_GENERATION");
    }
    
    /**
     * Regenerate care plan with updated information
     */
    @Transactional
    public CarePlanGenerationResponse regenerateCarePlan(
            UUID patientId, 
            CarePlanGenerationRequest updates) {
        
        log.info("Regenerating care plan for patient: {}", patientId);
        
        // Ensure patient ID matches
        updates.setPatientId(patientId);
        
        // Generate new care plan
        return generateCarePlan(updates);
    }
    
    // ==================== Private Helper Methods ====================
    
    /**
     * Build comprehensive context for care plan generation
     */
    private Map<String, String> buildCarePlanContext(
            CarePlanGenerationRequest request, Patient patient) {
        
        Map<String, String> context = new HashMap<>();
        
        // Patient demographics
        context.put("patientName", patient.getFirstName() + " " + patient.getLastName());
        
        if (patient.getDateOfBirth() != null) {
            int age = java.time.Period.between(
                    patient.getDateOfBirth(), 
                    java.time.LocalDate.now()
            ).getYears();
            context.put("age", String.valueOf(age));
            context.put("dateOfBirth", patient.getDateOfBirth().toString());
        }
        
        // Focus areas
        String focusAreasText = request.getFocusAreas().stream()
                .map(area -> area.getDisplayName() + " - " + area.getDescription())
                .collect(Collectors.joining("\n- ", "- ", ""));
        context.put("focusAreas", focusAreasText);
        
        // Timeframe
        context.put("timeframe", request.getTimeframe().getDisplayName());
        context.put("timeframeDuration", request.getTimeframe().getDuration());
        context.put("timeframeDescription", request.getTimeframe().getDescription());
        
        // Assessment data
        context.put("currentConditions", 
                request.getCurrentConditions() != null ? request.getCurrentConditions() : "Not provided");
        context.put("functionalStatus", 
                request.getFunctionalStatus() != null ? request.getFunctionalStatus() : "Not assessed");
        context.put("cognitiveStatus", 
                request.getCognitiveStatus() != null ? request.getCognitiveStatus() : "Not assessed");
        context.put("socialSupport", 
                request.getSocialSupport() != null ? request.getSocialSupport() : "Not documented");
        context.put("environmentalFactors", 
                request.getEnvironmentalFactors() != null ? request.getEnvironmentalFactors() : "Not specified");
        
        // Goals and preferences
        context.put("patientGoals", 
                request.getPatientGoals() != null ? request.getPatientGoals() : "To be determined with patient");
        context.put("familyPreferences", 
                request.getFamilyPreferences() != null ? request.getFamilyPreferences() : "To be discussed");
        
        // Additional assessment data
        if (request.getAssessmentData() != null && !request.getAssessmentData().isEmpty()) {
            request.getAssessmentData().forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    context.put(key, value);
                }
            });
        }
        
        // Additional notes
        if (request.getAdditionalNotes() != null && !request.getAdditionalNotes().isEmpty()) {
            context.put("additionalNotes", request.getAdditionalNotes());
        }
        
        log.debug("Built care plan context with {} variables", context.size());
        
        return context;
    }
    
    /**
     * Parse AI response into structured care plan sections
     */
    private CarePlanGenerationResponse parseCarePlanResponse(
            String aiResponse, 
            CarePlanGenerationRequest request,
            Patient patient) {
        
        CarePlanGenerationResponse response = new CarePlanGenerationResponse();
        
        // For now, we'll store the entire response
        // In production, you'd parse sections based on markers or use structured output
        
        // Try to extract sections if they're marked
        String[] sections = aiResponse.split("###");
        
        for (String section : sections) {
            section = section.trim();
            
            if (section.toLowerCase().startsWith("executive summary")) {
                response.setExecutiveSummary(extractSectionContent(section));
            } else if (section.toLowerCase().contains("medical management")) {
                response.setMedicalManagement(extractSectionContent(section));
            } else if (section.toLowerCase().contains("daily activities") || 
                       section.toLowerCase().contains("activities of daily living")) {
                response.setDailyActivities(extractSectionContent(section));
            } else if (section.toLowerCase().contains("nutrition")) {
                response.setNutritionPlan(extractSectionContent(section));
            } else if (section.toLowerCase().contains("exercise") || 
                       section.toLowerCase().contains("physical activity")) {
                response.setExercisePlan(extractSectionContent(section));
            } else if (section.toLowerCase().contains("safety") || 
                       section.toLowerCase().contains("fall prevention")) {
                response.setSafetyMeasures(extractSectionContent(section));
            } else if (section.toLowerCase().contains("cognitive") || 
                       section.toLowerCase().contains("mental health")) {
                response.setCognitiveSupport(extractSectionContent(section));
            } else if (section.toLowerCase().contains("social")) {
                response.setSocialEngagement(extractSectionContent(section));
            } else if (section.toLowerCase().contains("pain")) {
                response.setPainManagement(extractSectionContent(section));
            } else if (section.toLowerCase().contains("goals") || 
                       section.toLowerCase().contains("objectives")) {
                response.setGoalsAndObjectives(extractSectionContent(section));
            } else if (section.toLowerCase().contains("monitoring")) {
                response.setMonitoringPlan(extractSectionContent(section));
            } else if (section.toLowerCase().contains("caregiver")) {
                response.setCaregiverInstructions(extractSectionContent(section));
            } else if (section.toLowerCase().contains("emergency")) {
                response.setEmergencyProtocols(extractSectionContent(section));
            }
        }
        
        // If no sections were parsed, store the entire response in executive summary
        if (response.getExecutiveSummary() == null) {
            response.setExecutiveSummary(aiResponse);
        }
        
        return response;
    }
    
    /**
     * Extract content from a section (remove the header)
     */
    private String extractSectionContent(String section) {
        int firstNewline = section.indexOf('\n');
        if (firstNewline > 0 && firstNewline < section.length() - 1) {
            return section.substring(firstNewline + 1).trim();
        }
        return section.trim();
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
            
            return UUID.randomUUID(); // Placeholder
        }
        
        log.warn("No authenticated user found, using system UUID");
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
