package com.geriatriccare.dto.ai;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CarePlanGenerationResponse {
    
    private UUID id;
    
    private UUID patientId;
    
    private UUID carePlanId;
    
    private String patientName;
    
    private List<CarePlanFocusArea> focusAreas;
    
    private CarePlanTimeframe timeframe;
    
    // AI-generated sections
    private String executiveSummary;
    
    private String medicalManagement;
    
    private String dailyActivities;
    
    private String nutritionPlan;
    
    private String exercisePlan;
    
    private String safetyMeasures;
    
    private String cognitiveSupport;
    
    private String socialEngagement;
    
    private String painManagement;
    
    private String goalsAndObjectives;
    
    private String monitoringPlan;
    
    private String caregiverInstructions;
    
    private String emergencyProtocols;
    
    // Metadata
    private String aiModel;
    
    private String templateUsed;
    
    private String prompt;
    
    private Integer tokensUsed;
    
    private LocalDateTime generatedAt;
    
    private UUID generatedBy;
    
    private Boolean savedToDatabase;
    
    // Constructors
    public CarePlanGenerationResponse() {
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
    
    public UUID getCarePlanId() {
        return carePlanId;
    }
    
    public void setCarePlanId(UUID carePlanId) {
        this.carePlanId = carePlanId;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public List<CarePlanFocusArea> getFocusAreas() {
        return focusAreas;
    }
    
    public void setFocusAreas(List<CarePlanFocusArea> focusAreas) {
        this.focusAreas = focusAreas;
    }
    
    public CarePlanTimeframe getTimeframe() {
        return timeframe;
    }
    
    public void setTimeframe(CarePlanTimeframe timeframe) {
        this.timeframe = timeframe;
    }
    
    public String getExecutiveSummary() {
        return executiveSummary;
    }
    
    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }
    
    public String getMedicalManagement() {
        return medicalManagement;
    }
    
    public void setMedicalManagement(String medicalManagement) {
        this.medicalManagement = medicalManagement;
    }
    
    public String getDailyActivities() {
        return dailyActivities;
    }
    
    public void setDailyActivities(String dailyActivities) {
        this.dailyActivities = dailyActivities;
    }
    
    public String getNutritionPlan() {
        return nutritionPlan;
    }
    
    public void setNutritionPlan(String nutritionPlan) {
        this.nutritionPlan = nutritionPlan;
    }
    
    public String getExercisePlan() {
        return exercisePlan;
    }
    
    public void setExercisePlan(String exercisePlan) {
        this.exercisePlan = exercisePlan;
    }
    
    public String getSafetyMeasures() {
        return safetyMeasures;
    }
    
    public void setSafetyMeasures(String safetyMeasures) {
        this.safetyMeasures = safetyMeasures;
    }
    
    public String getCognitiveSupport() {
        return cognitiveSupport;
    }
    
    public void setCognitiveSupport(String cognitiveSupport) {
        this.cognitiveSupport = cognitiveSupport;
    }
    
    public String getSocialEngagement() {
        return socialEngagement;
    }
    
    public void setSocialEngagement(String socialEngagement) {
        this.socialEngagement = socialEngagement;
    }
    
    public String getPainManagement() {
        return painManagement;
    }
    
    public void setPainManagement(String painManagement) {
        this.painManagement = painManagement;
    }
    
    public String getGoalsAndObjectives() {
        return goalsAndObjectives;
    }
    
    public void setGoalsAndObjectives(String goalsAndObjectives) {
        this.goalsAndObjectives = goalsAndObjectives;
    }
    
    public String getMonitoringPlan() {
        return monitoringPlan;
    }
    
    public void setMonitoringPlan(String monitoringPlan) {
        this.monitoringPlan = monitoringPlan;
    }
    
    public String getCaregiverInstructions() {
        return caregiverInstructions;
    }
    
    public void setCaregiverInstructions(String caregiverInstructions) {
        this.caregiverInstructions = caregiverInstructions;
    }
    
    public String getEmergencyProtocols() {
        return emergencyProtocols;
    }
    
    public void setEmergencyProtocols(String emergencyProtocols) {
        this.emergencyProtocols = emergencyProtocols;
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
    
    public Boolean getSavedToDatabase() {
        return savedToDatabase;
    }
    
    public void setSavedToDatabase(Boolean savedToDatabase) {
        this.savedToDatabase = savedToDatabase;
    }
}
