package com.geriatriccare.enums;

/**
 * Data Sensitivity Enumeration
 * Classification of data sensitivity for audit and access control
 * From Sprint 5: Security & Compliance (HIPAA compliance)
 */
public enum DataSensitivity {
    /**
     * Public data - no restrictions
     */
    PUBLIC("Public", 0),
    
    /**
     * Internal data - accessible to authenticated users
     */
    INTERNAL("Internal", 1),
    
    /**
     * Confidential data - restricted access
     */
    CONFIDENTIAL("Confidential", 2),
    
    /**
     * Protected Health Information (PHI) - HIPAA protected
     * Requires special handling and audit logging
     */
    PHI("Protected Health Information", 3);

    private final String displayName;
    private final int level;

    DataSensitivity(String displayName, int level) {
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
     * Check if data requires encryption at rest
     */
    public boolean requiresEncryption() {
        return this == CONFIDENTIAL || this == PHI;
    }

    /**
     * Check if data access requires audit logging
     */
    public boolean requiresAuditLogging() {
        return this == PHI || this == CONFIDENTIAL;
    }

    /**
     * Check if this is PHI data (HIPAA compliance)
     */
    public boolean isPHI() {
        return this == PHI;
    }

    /**
     * Get retention period in years
     */
    public int getRetentionYears() {
        switch (this) {
            case PHI:
                return 7; // HIPAA requirement
            case CONFIDENTIAL:
                return 5;
            case INTERNAL:
                return 3;
            case PUBLIC:
            default:
                return 1;
        }
    }
}
