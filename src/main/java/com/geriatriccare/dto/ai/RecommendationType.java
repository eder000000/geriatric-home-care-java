package com.geriatriccare.dto.ai;

public enum RecommendationType {
    MEDICATION_RECOMMENDATION("Medication Recommendation", "AI-powered medication suggestions"),
    DRUG_INTERACTION_CHECK("Drug Interaction Check", "Analysis of potential drug interactions"),
    DOSAGE_CALCULATION("Dosage Calculation", "Age-appropriate dosage recommendations"),
    SYMPTOM_ANALYSIS("Symptom Analysis", "Analysis of patient symptoms"),
    ADVERSE_EVENT_ASSESSMENT("Adverse Event Assessment", "Evaluation of potential adverse events"),
    MEDICATION_REVIEW("Medication Review", "Comprehensive medication regimen review"),
    CARE_PLAN_GENERATION("Care Plan Generation", "Personalized care plan recommendations");
    
    private final String displayName;
    private final String description;
    
    RecommendationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
