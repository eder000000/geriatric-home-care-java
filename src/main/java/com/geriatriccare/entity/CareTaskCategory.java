package com.geriatriccare.entity;

public enum CareTaskCategory {
    MEDICATION("Medication Management"),
    PERSONAL_CARE("Personal Care"),
    NUTRITION("Nutrition & Hydration"),
    MOBILITY("Mobility & Exercise"),
    SAFETY("Safety Check"),
    SOCIAL("Social Interaction"),
    MEDICAL("Medical Care"),
    HYGIENE("Hygiene"),
    MENTAL_HEALTH("Mental Health"),
    EMERGENCY("Emergency Response");

    private final String displayName;

    CareTaskCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean requiresMedicalTraining() {
        return this == MEDICATION || this == MEDICAL || this == EMERGENCY;
    }

    public boolean isCritical() {
        return this == MEDICATION || this == SAFETY || this == EMERGENCY;
    }
}