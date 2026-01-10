package com.geriatriccare.dto.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SymptomAnalysisResponse {
    
    private UUID id;
    private UUID patientId;
    private String patientName;
    private String symptomsAnalyzed;
    private SymptomUrgencyLevel urgencyLevel;
    private String differentialDiagnosis;
    private List<String> possibleCauses;
    private List<String> redFlags;
    private String atypicalPresentations;
    private String medicationRelatedConcerns;
    private String immediateActions;
    private String monitoringRecommendations;
    private String followUpTimeframe;
    private String whenToSeekEmergencyCare;
    private String patientEducation;
    private String aiModel;
    private String templateUsed;
    private Integer tokensUsed;
    private LocalDateTime analyzedAt;
    private UUID analyzedBy;
    private Boolean savedToHistory;
    
    public SymptomAnalysisResponse() {
        this.possibleCauses = new ArrayList<>();
        this.redFlags = new ArrayList<>();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getSymptomsAnalyzed() { return symptomsAnalyzed; }
    public void setSymptomsAnalyzed(String symptomsAnalyzed) { this.symptomsAnalyzed = symptomsAnalyzed; }
    
    public SymptomUrgencyLevel getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(SymptomUrgencyLevel urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    
    public String getDifferentialDiagnosis() { return differentialDiagnosis; }
    public void setDifferentialDiagnosis(String differentialDiagnosis) { this.differentialDiagnosis = differentialDiagnosis; }
    
    public List<String> getPossibleCauses() { return possibleCauses; }
    public void setPossibleCauses(List<String> possibleCauses) { this.possibleCauses = possibleCauses; }
    
    public List<String> getRedFlags() { return redFlags; }
    public void setRedFlags(List<String> redFlags) { this.redFlags = redFlags; }
    
    public String getAtypicalPresentations() { return atypicalPresentations; }
    public void setAtypicalPresentations(String atypicalPresentations) { this.atypicalPresentations = atypicalPresentations; }
    
    public String getMedicationRelatedConcerns() { return medicationRelatedConcerns; }
    public void setMedicationRelatedConcerns(String medicationRelatedConcerns) { this.medicationRelatedConcerns = medicationRelatedConcerns; }
    
    public String getImmediateActions() { return immediateActions; }
    public void setImmediateActions(String immediateActions) { this.immediateActions = immediateActions; }
    
    public String getMonitoringRecommendations() { return monitoringRecommendations; }
    public void setMonitoringRecommendations(String monitoringRecommendations) { this.monitoringRecommendations = monitoringRecommendations; }
    
    public String getFollowUpTimeframe() { return followUpTimeframe; }
    public void setFollowUpTimeframe(String followUpTimeframe) { this.followUpTimeframe = followUpTimeframe; }
    
    public String getWhenToSeekEmergencyCare() { return whenToSeekEmergencyCare; }
    public void setWhenToSeekEmergencyCare(String whenToSeekEmergencyCare) { this.whenToSeekEmergencyCare = whenToSeekEmergencyCare; }
    
    public String getPatientEducation() { return patientEducation; }
    public void setPatientEducation(String patientEducation) { this.patientEducation = patientEducation; }
    
    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    
    public String getTemplateUsed() { return templateUsed; }
    public void setTemplateUsed(String templateUsed) { this.templateUsed = templateUsed; }
    
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
    
    public UUID getAnalyzedBy() { return analyzedBy; }
    public void setAnalyzedBy(UUID analyzedBy) { this.analyzedBy = analyzedBy; }
    
    public Boolean getSavedToHistory() { return savedToHistory; }
    public void setSavedToHistory(Boolean savedToHistory) { this.savedToHistory = savedToHistory; }
}
