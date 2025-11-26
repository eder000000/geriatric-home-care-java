package com.geriatriccare.entity;

public enum InteractionSeverity {
    MINOR("Minor - Monitor"),
    MODERATE("Moderate - Caution advised"),
    SEVERE("Severe - Avoid combination"),
    CONTRAINDICATED("Contraindicated - Do not combine");

    private final String displayName;

    InteractionSeverity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}