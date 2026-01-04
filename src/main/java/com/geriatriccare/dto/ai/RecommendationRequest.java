package com.geriatriccare.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RecommendationRequest {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Recommendation type is required")
    private RecommendationType type;
    
    @NotBlank(message = "Template name is required")
    private String templateName;
    
    @NotNull(message = "Context variables are required")
    private Map<String, String> contextVariables = new HashMap<>();
    
    private String additionalNotes;
    
    private Boolean saveToHistory = true;
    
    // Constructors
    public RecommendationRequest() {
    }
    
    public RecommendationRequest(UUID patientId, RecommendationType type, String templateName) {
        this.patientId = patientId;
        this.type = type;
        this.templateName = templateName;
    }
    
    // Getters and Setters
    public UUID getPatientId() {
        return patientId;
    }
    
    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }
    
    public RecommendationType getType() {
        return type;
    }
    
    public void setType(RecommendationType type) {
        this.type = type;
    }
    
    public String getTemplateName() {
        return templateName;
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    public Map<String, String> getContextVariables() {
        return contextVariables;
    }
    
    public void setContextVariables(Map<String, String> contextVariables) {
        this.contextVariables = contextVariables;
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
    
    public Boolean getSaveToHistory() {
        return saveToHistory;
    }
    
    public void setSaveToHistory(Boolean saveToHistory) {
        this.saveToHistory = saveToHistory;
    }
    
    // Helper method to add context variables
    public RecommendationRequest addContext(String key, String value) {
        this.contextVariables.put(key, value);
        return this;
    }
}
