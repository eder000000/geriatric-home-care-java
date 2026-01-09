package com.geriatriccare.dto.ai;

public class DrugInteraction {
    
    private String drug1;
    
    private String drug2;
    
    private InteractionType interactionType;
    
    private InteractionSeverity severity;
    
    private String mechanism;
    
    private String clinicalEffects;
    
    private String managementRecommendations;
    
    private String alternativeMedications;
    
    private String monitoringParameters;
    
    private String patientCounseling;
    
    private String references;
    
    // Constructors
    public DrugInteraction() {
    }
    
    // Getters and Setters
    public String getDrug1() {
        return drug1;
    }
    
    public void setDrug1(String drug1) {
        this.drug1 = drug1;
    }
    
    public String getDrug2() {
        return drug2;
    }
    
    public void setDrug2(String drug2) {
        this.drug2 = drug2;
    }
    
    public InteractionType getInteractionType() {
        return interactionType;
    }
    
    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
    }
    
    public InteractionSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(InteractionSeverity severity) {
        this.severity = severity;
    }
    
    public String getMechanism() {
        return mechanism;
    }
    
    public void setMechanism(String mechanism) {
        this.mechanism = mechanism;
    }
    
    public String getClinicalEffects() {
        return clinicalEffects;
    }
    
    public void setClinicalEffects(String clinicalEffects) {
        this.clinicalEffects = clinicalEffects;
    }
    
    public String getManagementRecommendations() {
        return managementRecommendations;
    }
    
    public void setManagementRecommendations(String managementRecommendations) {
        this.managementRecommendations = managementRecommendations;
    }
    
    public String getAlternativeMedications() {
        return alternativeMedications;
    }
    
    public void setAlternativeMedications(String alternativeMedications) {
        this.alternativeMedications = alternativeMedications;
    }
    
    public String getMonitoringParameters() {
        return monitoringParameters;
    }
    
    public void setMonitoringParameters(String monitoringParameters) {
        this.monitoringParameters = monitoringParameters;
    }
    
    public String getPatientCounseling() {
        return patientCounseling;
    }
    
    public void setPatientCounseling(String patientCounseling) {
        this.patientCounseling = patientCounseling;
    }
    
    public String getReferences() {
        return references;
    }
    
    public void setReferences(String references) {
        this.references = references;
    }
}
