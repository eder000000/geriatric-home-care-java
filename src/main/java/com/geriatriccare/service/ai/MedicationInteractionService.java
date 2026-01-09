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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MedicationInteractionService {
    
    private static final Logger log = LoggerFactory.getLogger(MedicationInteractionService.class);
    
    private final OpenAIService openAIService;
    private final PromptTemplateService promptTemplateService;
    private final AIAuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    
    @Autowired
    public MedicationInteractionService(
            OpenAIService openAIService,
            PromptTemplateService promptTemplateService,
            AIAuditLogRepository auditLogRepository,
            PatientRepository patientRepository) {
        this.openAIService = openAIService;
        this.promptTemplateService = promptTemplateService;
        this.auditLogRepository = auditLogRepository;
        this.patientRepository = patientRepository;
    }
    
    @Transactional
    public InteractionCheckResponse checkInteractions(InteractionCheckRequest request) {
        log.info("Checking medication interactions for patient: {}", request.getPatientId());
        
        // Validate patient
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found: " + request.getPatientId()));
        
        // Get template
        PromptTemplateResponse template = promptTemplateService
                .getLatestTemplateByName("Drug Interaction Checker");
        
        // Build context
        Map<String, String> context = buildInteractionContext(request, patient);
        
        // Render prompt
        String renderedPrompt = promptTemplateService.renderTemplate(template.getTemplate(), context);
        
        // Call OpenAI
        String aiResponse;
        try {
            OpenAIResponse openAIResponse = openAIService.generateCompletion(
                    renderedPrompt, "Drug Interaction Check");
            aiResponse = openAIResponse.getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check interactions: " + e.getMessage(), e);
        }
        
        // Parse response
        InteractionCheckResponse response = parseInteractionResponse(aiResponse, request, patient);
        
        // Audit log
        if (request.getSaveToHistory()) {
            AIAuditLog auditLog = createAuditLog(
                    request.getPatientId(),
                    "DRUG_INTERACTION_CHECK",
                    renderedPrompt,
                    aiResponse
            );
            response.setId(auditLog.getId());
        }
        
        // Set metadata
        response.setPatientId(patient.getId());
        response.setPatientName(patient.getFirstName() + " " + patient.getLastName());
        response.setCheckedAt(LocalDateTime.now());
        response.setCheckedBy(getCurrentUserId());
        response.setSavedToHistory(request.getSaveToHistory());
        response.setAiModel("gpt-4");
        response.setTemplateUsed(template.getName());
        
        log.info("Found {} interactions for patient: {}", response.getTotalInteractionsFound(), request.getPatientId());
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<AIAuditLog> getInteractionHistory(UUID patientId) {
        log.info("Retrieving interaction check history for patient: {}", patientId);
        
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found: " + patientId);
        }
        
        return auditLogRepository.findByPatientIdAndRequestTypeOrderByTimestampDesc(
                patientId, "DRUG_INTERACTION_CHECK");
    }
    
    private Map<String, String> buildInteractionContext(InteractionCheckRequest request, Patient patient) {
        Map<String, String> context = new HashMap<>();
        
        context.put("patientName", patient.getFirstName() + " " + patient.getLastName());
        
        if (patient.getDateOfBirth() != null) {
            int age = java.time.Period.between(patient.getDateOfBirth(), java.time.LocalDate.now()).getYears();
            context.put("age", String.valueOf(age));
        }
        
        // Medications
        String medicationList = String.join("\n- ", request.getMedications());
        context.put("medicationList", "- " + medicationList);
        
        // OTC meds
        String otcList = request.getOtcMedications() != null && !request.getOtcMedications().isEmpty()
                ? String.join(", ", request.getOtcMedications())
                : "None reported";
        context.put("otcMedications", otcList);
        
        // Supplements
        String suppList = request.getSupplements() != null && !request.getSupplements().isEmpty()
                ? String.join(", ", request.getSupplements())
                : "None reported";
        context.put("supplements", suppList);
        
        // Recent changes
        String recentChanges = request.getRecentMedicationChanges() != null && !request.getRecentMedicationChanges().isEmpty()
                ? String.join(", ", request.getRecentMedicationChanges())
                : "None reported";
        context.put("recentChanges", recentChanges);
        
        // Medical conditions
        if (request.getMedicalConditions() != null && !request.getMedicalConditions().isEmpty()) {
            context.put("medicalConditions", String.join(", ", request.getMedicalConditions()));
        }
        
        // Dietary considerations
        if (request.getDietaryConsiderations() != null) {
            context.put("dietaryConsiderations", request.getDietaryConsiderations());
        }
        
        // Additional notes
        if (request.getAdditionalNotes() != null) {
            context.put("additionalNotes", request.getAdditionalNotes());
        }
        
        return context;
    }
    
    private InteractionCheckResponse parseInteractionResponse(
            String aiResponse, InteractionCheckRequest request, Patient patient) {
        
        InteractionCheckResponse response = new InteractionCheckResponse();
        
        // Combine all medications checked
        List<String> allMeds = new ArrayList<>(request.getMedications());
        if (request.getOtcMedications() != null) allMeds.addAll(request.getOtcMedications());
        if (request.getSupplements() != null) allMeds.addAll(request.getSupplements());
        response.setMedicationsChecked(allMeds);
        
        // Parse interactions - simple parsing (production would use structured output)
        List<DrugInteraction> interactions = new ArrayList<>();
        
        // Split by sections or markers
        String[] lines = aiResponse.split("\n");
        DrugInteraction currentInteraction = null;
        
        for (String line : lines) {
            line = line.trim();
            
            // Detect new interaction
            if (line.toLowerCase().contains("interaction:") || 
                line.toLowerCase().matches(".*\\d+\\..*") ||
                line.startsWith("**") && line.contains("vs")) {
                
                if (currentInteraction != null) {
                    interactions.add(currentInteraction);
                }
                currentInteraction = new DrugInteraction();
                currentInteraction.setInteractionType(InteractionType.DRUG_DRUG);
            }
            
            // Parse severity
            if (line.toLowerCase().contains("severity:")) {
                if (currentInteraction != null) {
                    if (line.toLowerCase().contains("contraindicated")) {
                        currentInteraction.setSeverity(InteractionSeverity.CONTRAINDICATED);
                    } else if (line.toLowerCase().contains("major")) {
                        currentInteraction.setSeverity(InteractionSeverity.MAJOR);
                    } else if (line.toLowerCase().contains("moderate")) {
                        currentInteraction.setSeverity(InteractionSeverity.MODERATE);
                    } else if (line.toLowerCase().contains("minor")) {
                        currentInteraction.setSeverity(InteractionSeverity.MINOR);
                    }
                }
            }
            
            // Parse mechanism
            if (line.toLowerCase().contains("mechanism:")) {
                if (currentInteraction != null) {
                    currentInteraction.setMechanism(line.substring(line.indexOf(":") + 1).trim());
                }
            }
            
            // Parse clinical effects
            if (line.toLowerCase().contains("clinical") || line.toLowerCase().contains("effect")) {
                if (currentInteraction != null && currentInteraction.getClinicalEffects() == null) {
                    currentInteraction.setClinicalEffects(line);
                }
            }
            
            // Parse management
            if (line.toLowerCase().contains("management:") || line.toLowerCase().contains("recommendation:")) {
                if (currentInteraction != null) {
                    currentInteraction.setManagementRecommendations(line.substring(line.indexOf(":") + 1).trim());
                }
            }
        }
        
        // Add last interaction
        if (currentInteraction != null) {
            interactions.add(currentInteraction);
        }
        
        // If parsing failed, store full response in summary
        if (interactions.isEmpty()) {
            response.setSummary(aiResponse);
        } else {
            response.setInteractions(interactions);
        }
        
        // Count by severity
        for (DrugInteraction interaction : interactions) {
            if (interaction.getSeverity() != null) {
                switch (interaction.getSeverity()) {
                    case CONTRAINDICATED:
                        response.setContraindicatedCount(response.getContraindicatedCount() + 1);
                        break;
                    case MAJOR:
                        response.setMajorCount(response.getMajorCount() + 1);
                        break;
                    case MODERATE:
                        response.setModerateCount(response.getModerateCount() + 1);
                        break;
                    case MINOR:
                        response.setMinorCount(response.getMinorCount() + 1);
                        break;
                }
            }
        }
        
        response.setTotalInteractionsFound(interactions.size());
        
        // Overall risk assessment
        if (response.getContraindicatedCount() > 0) {
            response.setOverallRiskAssessment("CRITICAL - Contraindicated combinations found");
            response.setPriorityActions("IMMEDIATE ACTION REQUIRED - Review contraindicated medications");
        } else if (response.getMajorCount() > 0) {
            response.setOverallRiskAssessment("HIGH RISK - Major interactions detected");
            response.setPriorityActions("Urgent review needed - Consider alternative therapy");
        } else if (response.getModerateCount() > 0) {
            response.setOverallRiskAssessment("MODERATE RISK - Close monitoring required");
            response.setPriorityActions("Schedule follow-up and implement monitoring plan");
        } else if (response.getMinorCount() > 0) {
            response.setOverallRiskAssessment("LOW RISK - Minor interactions noted");
            response.setPriorityActions("Awareness and routine monitoring sufficient");
        } else {
            response.setOverallRiskAssessment("NO SIGNIFICANT INTERACTIONS DETECTED");
            response.setPriorityActions("Continue current regimen with routine monitoring");
        }
        
        return response;
    }
    
    private AIAuditLog createAuditLog(UUID patientId, String requestType, String prompt, String response) {
        AIAuditLog auditLog = new AIAuditLog();
        auditLog.setPatientId(patientId);
        auditLog.setUserId(getCurrentUserId());
        auditLog.setRequestType(requestType);
        auditLog.setPrompt(prompt);
        auditLog.setResponse(response);
        auditLog.setSuccess(true);
        auditLog.setTimestamp(LocalDateTime.now());
        
        return auditLogRepository.save(auditLog);
    }
    
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            return UUID.randomUUID();
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
