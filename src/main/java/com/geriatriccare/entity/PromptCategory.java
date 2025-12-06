package com.geriatriccare.entity;

public enum PromptCategory {
    MEDICATION_RECOMMENDATION("Medication Recommendation", "Generate medication recommendations based on patient profile"),
    DRUG_INTERACTION("Drug Interaction Analysis", "Analyze potential drug interactions"),
    DOSAGE_CALCULATION("Dosage Calculation", "Calculate appropriate medication dosages"),
    CARE_PLAN("Care Plan Generation", "Generate personalized care plans"),
    SYMPTOM_ANALYSIS("Symptom Analysis", "Analyze patient symptoms and suggest actions"),
    ADVERSE_EVENT("Adverse Event Detection", "Detect and analyze adverse drug events"),
    MEDICATION_REVIEW("Medication Review", "Comprehensive medication regimen review");

    private final String displayName;
    private final String description;

    PromptCategory(String displayName, String description) {
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