package com.geriatriccare.dto.ai;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CarePlanGenerationRequest {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Focus areas are required")
    private List<CarePlanFocusArea> focusAreas;
    
    private Map<String, String> assessmentData;
    
    private String currentConditions;
    
    private String functionalStatus;
    
    private String cognitiveStatus;
    
    private String socialSupport;
    
    private String environmentalFactors;
    
    private String patientGoals;
    
    private String familyPreferences;
    
    private CarePlanTimeframe timeframe;
    
    private UUID existingCarePlanId;
    
    private String additionalNotes;
    
    private Boolean saveToDatabase;
    
    // Constructors
    public CarePlanGenerationRequest() {
        this.assessmentData = new HashMap<>();
        this.saveToDatabase = true;
        this.timeframe = CarePlanTimeframe.SHORT_TERM;
    }
    
    // Helper method for adding assessment data
    public CarePlanGenerationRequest addAssessmentData(String key, String value) {
        this.assessmentData.put(key, value);
        return this;
    }
    
    // Getters and Setters
    public UUID getPatientId() {
        return patientId;
    }
    
    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }
    
    public List<CarePlanFocusArea> getFocusAreas() {
        return focusAreas;
    }
    
    public void setFocusAreas(List<CarePlanFocusArea> focusAreas) {
        this.focusAreas = focusAreas;
    }
    
    public Map<String, String> getAssessmentData() {
        return assessmentData;
    }
    
    public void setAssessmentData(Map<String, String> assessmentData) {
        this.assessmentData = assessmentData;
    }
    
    public String getCurrentConditions() {
        return currentConditions;
    }
    
    public void setCurrentConditions(String currentConditions) {
        this.currentConditions = currentConditions;
    }
    
    public String getFunctionalStatus() {
        return functionalStatus;
    }
    
    public void setFunctionalStatus(String functionalStatus) {
        this.functionalStatus = functionalStatus;
    }
    
    public String getCognitiveStatus() {
        return cognitiveStatus;
    }
    
    public void setCognitiveStatus(String cognitiveStatus) {
        this.cognitiveStatus = cognitiveStatus;
    }
    
    public String getSocialSupport() {
        return socialSupport;
    }
    
    public void setSocialSupport(String socialSupport) {
        this.socialSupport = socialSupport;
    }
    
    public String getEnvironmentalFactors() {
        return environmentalFactors;
    }
    
    public void setEnvironmentalFactors(String environmentalFactors) {
        this.environmentalFactors = environmentalFactors;
    }
    
    public String getPatientGoals() {
        return patientGoals;
    }
    
    public void setPatientGoals(String patientGoals) {
        this.patientGoals = patientGoals;
    }
    
    public String getFamilyPreferences() {
        return familyPreferences;
    }
    
    public void setFamilyPreferences(String familyPreferences) {
        this.familyPreferences = familyPreferences;
    }
    
    public CarePlanTimeframe getTimeframe() {
        return timeframe;
    }
    
    public void setTimeframe(CarePlanTimeframe timeframe) {
        this.timeframe = timeframe;
    }
    
    public UUID getExistingCarePlanId() {
        return existingCarePlanId;
    }
    
    public void setExistingCarePlanId(UUID existingCarePlanId) {
        this.existingCarePlanId = existingCarePlanId;
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
    
    public Boolean getSaveToDatabase() {
        return saveToDatabase;
    }
    
    public void setSaveToDatabase(Boolean saveToDatabase) {
        this.saveToDatabase = saveToDatabase;
    }
}
