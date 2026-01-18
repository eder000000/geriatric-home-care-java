package com.geriatriccare.dto.security;

public enum AuditSeverity {
    INFO("Info", "Informational event", 1),
    WARNING("Warning", "Warning - requires attention", 2),
    CRITICAL("Critical", "Critical event - immediate attention required", 3),
    SECURITY_INCIDENT("Security Incident", "Security incident - investigation required", 4);
    
    private final String displayName;
    private final String description;
    private final int level;
    
    AuditSeverity(String displayName, String description, int level) {
        this.displayName = displayName;
        this.description = description;
        this.level = level;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getLevel() { return level; }
}
