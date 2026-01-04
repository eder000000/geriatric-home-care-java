package com.geriatriccare.dto.ai;

import java.time.LocalDateTime;
import java.util.UUID;

public class RecommendationResponse {
    
    private UUID id;
    private UUID patientId;
    private RecommendationType type;
    private String templateUsed;
    private String recommendation;
    private String prompt;
    private Integer tokensUsed;
    private String model;
    private LocalDateTime generatedAt;
    private UUID generatedBy;
    private String confidence;
    private Boolean savedToHistory;
    
    // Constructors
    public RecommendationResponse() {
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
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
    
    public String getTemplateUsed() {
        return templateUsed;
    }
    
    public void setTemplateUsed(String templateUsed) {
        this.templateUsed = templateUsed;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public Integer getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public UUID getGeneratedBy() {
        return generatedBy;
    }
    
    public void setGeneratedBy(UUID generatedBy) {
        this.generatedBy = generatedBy;
    }
    
    public String getConfidence() {
        return confidence;
    }
    
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
    
    public Boolean getSavedToHistory() {
        return savedToHistory;
    }
    
    public void setSavedToHistory(Boolean savedToHistory) {
        this.savedToHistory = savedToHistory;
    }
}
