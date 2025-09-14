package com.geriatriccare.entity;

/**
 * Enum defining user roles in the geriatric care system.
 * Each role has specific permissions and access levels.
 */

public enum UserRole {
     /**
     * System administrator with full access to all features
     */
    ADMIN("Administrator"),
    
    /**
     * Professional caregiver who provides direct patient care
     */
    CAREGIVER("Caregiver"),
    
    /**
     * Family member with limited access to patient information
     */
    FAMILY("Family Member"),
    
    /**
     * Patient with access to their own information
     */
    PATIENT("Patient");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this role has administrative privileges
     */
    public boolean hasAdminPrivileges() {
        return this == ADMIN;
    }

    /**
     * Check if this role can provide care
     */
    public boolean canProvideCare() {
        return this == ADMIN || this == CAREGIVER;
    }

    /**
     * Check if this role can view patient data
     */
    public boolean canViewPatientData() {
        return this == ADMIN || this == CAREGIVER || this == FAMILY;
    }
}
