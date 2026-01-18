package com.geriatriccare.dto.security;

public enum DataSensitivity {
    PUBLIC("Public", "Non-sensitive public information"),
    INTERNAL("Internal", "Internal use only"),
    CONFIDENTIAL("Confidential", "Confidential business information"),
    PHI("Protected Health Information", "HIPAA-protected patient health information");
    
    private final String displayName;
    private final String description;
    
    DataSensitivity(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
