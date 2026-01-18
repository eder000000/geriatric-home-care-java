package com.geriatriccare.dto.security;

public enum AuditEventSeverity {
    INFO("Informational", "Normal operational event", 1),
    LOW("Low", "Minor event requiring awareness", 2),
    MEDIUM("Medium", "Moderate event requiring attention", 3),
    HIGH("High", "Significant event requiring review", 4),
    CRITICAL("Critical", "Critical security event requiring immediate action", 5);
    
    private final String displayName;
    private final String description;
    private final int level;
    
    AuditEventSeverity(String displayName, String description, int level) {
        this.displayName = displayName;
        this.description = description;
        this.level = level;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getLevel() { return level; }
}
