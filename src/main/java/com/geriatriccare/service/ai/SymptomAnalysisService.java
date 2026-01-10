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

@Service
public class SymptomAnalysisService {
    
    private static final Logger log = LoggerFactory.getLogger(SymptomAnalysisService.class);
    
    private final OpenAIService openAIService;
    private final PromptTemplateService promptTemplateService;
    private final AIAuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    
    @Autowired
    public SymptomAnalysisService(
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
    public SymptomAnalysisResponse analyzeSymptoms(SymptomAnalysisRequest request) {
        log.info("Analyzing symptoms for patient: {}", request.getPatientId());
        
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found: " + request.getPatientId()));
        
        PromptTemplateResponse template = promptTemplateService
                .getLatestTemplateByName("Geriatric Symptom Analyzer");
        
        Map<String, String> context = buildSymptomContext(request, patient);
        String renderedPrompt = promptTemplateService.renderTemplate(template.getTemplate(), context);
        
        String aiResponse;
        try {
            OpenAIResponse openAIResponse = openAIService.generateCompletion(
                    renderedPrompt, "Symptom Analysis");
            aiResponse = openAIResponse.getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze symptoms: " + e.getMessage(), e);
        }
        
        SymptomAnalysisResponse response = parseSymptomResponse(aiResponse, request, patient);
        
        if (request.getSaveToHistory()) {
            AIAuditLog auditLog = createAuditLog(
                    request.getPatientId(),
                    "SYMPTOM_ANALYSIS",
                    renderedPrompt,
                    aiResponse
            );
            response.setId(auditLog.getId());
        }
        
        response.setPatientId(patient.getId());
        response.setPatientName(patient.getFirstName() + " " + patient.getLastName());
        response.setSymptomsAnalyzed(request.getSymptoms());
        response.setAnalyzedAt(LocalDateTime.now());
        response.setAnalyzedBy(getCurrentUserId());
        response.setSavedToHistory(request.getSaveToHistory());
        response.setAiModel("gpt-4");
        response.setTemplateUsed(template.getName());
        
        log.info("Symptom analysis complete - urgency: {}", response.getUrgencyLevel());
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<AIAuditLog> getSymptomAnalysisHistory(UUID patientId) {
        log.info("Retrieving symptom analysis history for patient: {}", patientId);
        
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found: " + patientId);
        }
        
        return auditLogRepository.findByPatientIdAndRequestTypeOrderByTimestampDesc(
                patientId, "SYMPTOM_ANALYSIS");
    }
    
    private Map<String, String> buildSymptomContext(SymptomAnalysisRequest request, Patient patient) {
        Map<String, String> context = new HashMap<>();
        
        context.put("patientName", patient.getFirstName() + " " + patient.getLastName());
        
        if (patient.getDateOfBirth() != null) {
            int age = java.time.Period.between(patient.getDateOfBirth(), java.time.LocalDate.now()).getYears();
            context.put("age", String.valueOf(age));
        }
        
        context.put("symptoms", request.getSymptoms());
        context.put("duration", request.getDuration() != null ? request.getDuration() : "Not specified");
        context.put("severity", request.getSeverity() != null ? String.valueOf(request.getSeverity()) : "Not rated");
        context.put("onset", request.getOnset() != null ? request.getOnset() : "Not specified");
        
        String diagnoses = request.getCurrentDiagnoses() != null && !request.getCurrentDiagnoses().isEmpty()
                ? String.join(", ", request.getCurrentDiagnoses())
                : "None documented";
        context.put("diagnoses", diagnoses);
        
        String medications = request.getCurrentMedications() != null && !request.getCurrentMedications().isEmpty()
                ? String.join(", ", request.getCurrentMedications())
                : "None documented";
        context.put("medications", medications);
        
        String recentMedChanges = request.getRecentMedicationChanges() != null && !request.getRecentMedicationChanges().isEmpty()
                ? String.join(", ", request.getRecentMedicationChanges())
                : "None reported";
        context.put("recentMedChanges", recentMedChanges);
        
        context.put("vitalSigns", request.getVitalSigns() != null ? request.getVitalSigns() : "Not available");
        
        if (request.getAssociatedSymptoms() != null) {
            context.put("associatedSymptoms", request.getAssociatedSymptoms());
        }
        
        if (request.getAdditionalNotes() != null) {
            context.put("additionalNotes", request.getAdditionalNotes());
        }
        
        return context;
    }
    
    private SymptomAnalysisResponse parseSymptomResponse(
            String aiResponse, SymptomAnalysisRequest request, Patient patient) {
        
        SymptomAnalysisResponse response = new SymptomAnalysisResponse();
        
        // Parse urgency level
        String lowerResponse = aiResponse.toLowerCase();
        if (lowerResponse.contains("emergency") || lowerResponse.contains("911") || lowerResponse.contains("life-threatening")) {
            response.setUrgencyLevel(SymptomUrgencyLevel.EMERGENCY);
        } else if (lowerResponse.contains("urgent") && !lowerResponse.contains("non-urgent")) {
            response.setUrgencyLevel(SymptomUrgencyLevel.URGENT);
        } else if (lowerResponse.contains("semi-urgent") || lowerResponse.contains("within 24")) {
            response.setUrgencyLevel(SymptomUrgencyLevel.SEMI_URGENT);
        } else {
            response.setUrgencyLevel(SymptomUrgencyLevel.ROUTINE);
        }
        
        // Store full response in appropriate fields
        response.setDifferentialDiagnosis(aiResponse);
        
        // Extract red flags
        List<String> redFlags = new ArrayList<>();
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("red flag") || 
                line.toLowerCase().contains("warning sign") ||
                line.toLowerCase().contains("immediate attention")) {
                redFlags.add(line.trim());
            }
        }
        response.setRedFlags(redFlags);
        
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
