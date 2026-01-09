package com.geriatriccare.dto.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InteractionCheckResponse {
    
    private UUID id;
    
    private UUID patientId;
    
    private String patientName;
    
    private List<String> medicationsChecked;
    
    private Integer totalInteractionsFound;
    
    private Integer contraindicatedCount;
    
    private Integer majorCount;
    
    private Integer moderateCount;
    
    private Integer minorCount;
    
    private List<DrugInteraction> interactions;
    
    private String overallRiskAssessment;
    
    private String priorityActions;
    
    private String summary;
    
    private String aiModel;
    
    private String templateUsed;
    
    private Integer tokensUsed;
    
    private LocalDateTime checkedAt;
    
    private UUID checkedBy;
    
    private Boolean savedToHistory;
    
    // Constructors
    public InteractionCheckResponse() {
        this.interactions = new ArrayList<>();
        this.medicationsChecked = new ArrayList<>();
        this.totalInteractionsFound = 0;
        this.contraindicatedCount = 0;
        this.majorCount = 0;
        this.moderateCount = 0;
        this.minorCount = 0;
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
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public List<String> getMedicationsChecked() {
        return medicationsChecked;
    }
    
    public void setMedicationsChecked(List<String> medicationsChecked) {
        this.medicationsChecked = medicationsChecked;
    }
    
    public Integer getTotalInteractionsFound() {
        return totalInteractionsFound;
    }
    
    public void setTotalInteractionsFound(Integer totalInteractionsFound) {
        this.totalInteractionsFound = totalInteractionsFound;
    }
    
    public Integer getContraindicatedCount() {
        return contraindicatedCount;
    }
    
    public void setContraindicatedCount(Integer contraindicatedCount) {
        this.contraindicatedCount = contraindicatedCount;
    }
    
    public Integer getMajorCount() {
        return majorCount;
    }
    
    public void setMajorCount(Integer majorCount) {
        this.majorCount = majorCount;
    }
    
    public Integer getModerateCount() {
        return moderateCount;
    }
    
    public void setModerateCount(Integer moderateCount) {
        this.moderateCount = moderateCount;
    }
    
    public Integer getMinorCount() {
        return minorCount;
    }
    
    public void setMinorCount(Integer minorCount) {
        this.minorCount = minorCount;
    }
    
    public List<DrugInteraction> getInteractions() {
        return interactions;
    }
    
    public void setInteractions(List<DrugInteraction> interactions) {
        this.interactions = interactions;
    }
    
    public String getOverallRiskAssessment() {
        return overallRiskAssessment;
    }
    
    public void setOverallRiskAssessment(String overallRiskAssessment) {
        this.overallRiskAssessment = overallRiskAssessment;
    }
    
    public String getPriorityActions() {
        return priorityActions;
    }
    
    public void setPriorityActions(String priorityActions) {
        this.priorityActions = priorityActions;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getAiModel() {
        return aiModel;
    }
    
    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }
    
    public String getTemplateUsed() {
        return templateUsed;
    }
    
    public void setTemplateUsed(String templateUsed) {
        this.templateUsed = templateUsed;
    }
    
    public Integer getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
    
    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }
    
    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }
    
    public UUID getCheckedBy() {
        return checkedBy;
    }
    
    public void setCheckedBy(UUID checkedBy) {
        this.checkedBy = checkedBy;
    }
    
    public Boolean getSavedToHistory() {
        return savedToHistory;
    }
    
    public void setSavedToHistory(Boolean savedToHistory) {
        this.savedToHistory = savedToHistory;
    }
}
