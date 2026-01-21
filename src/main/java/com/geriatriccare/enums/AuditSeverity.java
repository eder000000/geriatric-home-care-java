package com.geriatriccare.enums;

/**
 * Audit Severity Enumeration
 * Severity levels for audit events
 * From Sprint 5: Security & Compliance
 */
public enum AuditSeverity {
    /**
     * Informational events - normal operations
     */
    INFO("Informational", 1),
    
    /**
     * Warning events - potential issues
     */
    WARNING("Warning", 2),
    
    /**
     * Error events - operational errors
     */
    ERROR("Error", 3),
    
    /**
     * Critical events - security incidents, data breaches
     */
    CRITICAL("Critical", 4);

    private final String displayName;
    private final int level;

    AuditSeverity(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Check if this severity is higher than another
     */
    public boolean isHigherThan(AuditSeverity other) {
        return this.level > other.level;
    }

    /**
     * Check if this severity requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return this == ERROR || this == CRITICAL;
    }
}
