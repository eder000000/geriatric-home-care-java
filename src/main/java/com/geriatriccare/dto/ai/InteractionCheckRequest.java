package com.geriatriccare.dto.ai;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InteractionCheckRequest {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Medication list is required")
    private List<String> medications;
    
    private List<String> otcMedications;
    
    private List<String> supplements;
    
    private List<String> medicalConditions;
    
    private List<String> recentMedicationChanges;
    
    private String dietaryConsiderations;
    
    private Boolean includeMinorInteractions;
    
    private Boolean checkFoodInteractions;
    
    private Boolean checkDiseaseInteractions;
    
    private String additionalNotes;
    
    private Boolean saveToHistory;
    
    // Constructors
    public InteractionCheckRequest() {
        this.medications = new ArrayList<>();
        this.otcMedications = new ArrayList<>();
        this.supplements = new ArrayList<>();
        this.medicalConditions = new ArrayList<>();
        this.recentMedicationChanges = new ArrayList<>();
        this.includeMinorInteractions = false;
        this.checkFoodInteractions = true;
        this.checkDiseaseInteractions = true;
        this.saveToHistory = true;
    }
    
    // Getters and Setters
    public UUID getPatientId() {
        return patientId;
    }
    
    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }
    
    public List<String> getMedications() {
        return medications;
    }
    
    public void setMedications(List<String> medications) {
        this.medications = medications;
    }
    
    public List<String> getOtcMedications() {
        return otcMedications;
    }
    
    public void setOtcMedications(List<String> otcMedications) {
        this.otcMedications = otcMedications;
    }
    
    public List<String> getSupplements() {
        return supplements;
    }
    
    public void setSupplements(List<String> supplements) {
        this.supplements = supplements;
    }
    
    public List<String> getMedicalConditions() {
        return medicalConditions;
    }
    
    public void setMedicalConditions(List<String> medicalConditions) {
        this.medicalConditions = medicalConditions;
    }
    
    public List<String> getRecentMedicationChanges() {
        return recentMedicationChanges;
    }
    
    public void setRecentMedicationChanges(List<String> recentMedicationChanges) {
        this.recentMedicationChanges = recentMedicationChanges;
    }
    
    public String getDietaryConsiderations() {
        return dietaryConsiderations;
    }
    
    public void setDietaryConsiderations(String dietaryConsiderations) {
        this.dietaryConsiderations = dietaryConsiderations;
    }
    
    public Boolean getIncludeMinorInteractions() {
        return includeMinorInteractions;
    }
    
    public void setIncludeMinorInteractions(Boolean includeMinorInteractions) {
        this.includeMinorInteractions = includeMinorInteractions;
    }
    
    public Boolean getCheckFoodInteractions() {
        return checkFoodInteractions;
    }
    
    public void setCheckFoodInteractions(Boolean checkFoodInteractions) {
        this.checkFoodInteractions = checkFoodInteractions;
    }
    
    public Boolean getCheckDiseaseInteractions() {
        return checkDiseaseInteractions;
    }
    
    public void setCheckDiseaseInteractions(Boolean checkDiseaseInteractions) {
        this.checkDiseaseInteractions = checkDiseaseInteractions;
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
}
