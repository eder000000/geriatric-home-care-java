package com.geriatriccare.dto.security;

public enum MFAStatus {
    DISABLED("Disabled", "MFA is not enabled"),
    PENDING_SETUP("Pending Setup", "MFA setup initiated but not completed"),
    ENABLED("Enabled", "MFA is active and required"),
    TEMPORARILY_DISABLED("Temporarily Disabled", "MFA temporarily disabled by admin");
    
    private final String displayName;
    private final String description;
    
    MFAStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
