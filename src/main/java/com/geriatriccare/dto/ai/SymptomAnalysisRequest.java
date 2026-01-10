package com.geriatriccare.dto.ai;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SymptomAnalysisRequest {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Symptoms description is required")
    private String symptoms;
    
    private String duration;
    private Integer severity; // 1-10 scale
    private String onset; // sudden, gradual, etc.
    private List<String> currentDiagnoses;
    private List<String> currentMedications;
    private List<String> recentMedicationChanges;
    private String vitalSigns;
    private String associatedSymptoms;
    private String alleviatingFactors;
    private String aggravatingFactors;
    private String additionalNotes;
    private Boolean saveToHistory;
    
    public SymptomAnalysisRequest() {
        this.currentDiagnoses = new ArrayList<>();
        this.currentMedications = new ArrayList<>();
        this.recentMedicationChanges = new ArrayList<>();
        this.saveToHistory = true;
    }
    
    // Getters and Setters
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    
    public Integer getSeverity() { return severity; }
    public void setSeverity(Integer severity) { this.severity = severity; }
    
    public String getOnset() { return onset; }
    public void setOnset(String onset) { this.onset = onset; }
    
    public List<String> getCurrentDiagnoses() { return currentDiagnoses; }
    public void setCurrentDiagnoses(List<String> currentDiagnoses) { this.currentDiagnoses = currentDiagnoses; }
    
    public List<String> getCurrentMedications() { return currentMedications; }
    public void setCurrentMedications(List<String> currentMedications) { this.currentMedications = currentMedications; }
    
    public List<String> getRecentMedicationChanges() { return recentMedicationChanges; }
    public void setRecentMedicationChanges(List<String> recentMedicationChanges) { this.recentMedicationChanges = recentMedicationChanges; }
    
    public String getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(String vitalSigns) { this.vitalSigns = vitalSigns; }
    
    public String getAssociatedSymptoms() { return associatedSymptoms; }
    public void setAssociatedSymptoms(String associatedSymptoms) { this.associatedSymptoms = associatedSymptoms; }
    
    public String getAlleviatingFactors() { return alleviatingFactors; }
    public void setAlleviatingFactors(String alleviatingFactors) { this.alleviatingFactors = alleviatingFactors; }
    
    public String getAggravatingFactors() { return aggravatingFactors; }
    public void setAggravatingFactors(String aggravatingFactors) { this.aggravatingFactors = aggravatingFactors; }
    
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    
    public Boolean getSaveToHistory() { return saveToHistory; }
    public void setSaveToHistory(Boolean saveToHistory) { this.saveToHistory = saveToHistory; }
}
