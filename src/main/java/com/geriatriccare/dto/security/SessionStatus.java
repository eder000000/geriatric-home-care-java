package com.geriatriccare.dto.security;

public enum SessionStatus {
    ACTIVE("Active", "Session is currently active"),
    EXPIRED("Expired", "Session expired due to inactivity"),
    REVOKED("Revoked", "Session manually revoked by user or admin"),
    LOGGED_OUT("Logged Out", "User explicitly logged out");
    
    private final String displayName;
    private final String description;
    
    SessionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
