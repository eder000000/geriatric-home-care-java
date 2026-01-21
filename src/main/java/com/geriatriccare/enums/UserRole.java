package com.geriatriccare.enums;

/**
 * User Role Enumeration
 * Defines the five primary system roles as per document requirements
 * 
 * Role Hierarchy (from highest to lowest):
 * 1. ADMIN - Full system access
 * 2. PHYSICIAN - Medical care and diagnoses
 * 3. CAREGIVER - Daily care and medication administration
 * 4. FAMILY - Patient monitoring and oversight
 * 5. PATIENT - Self-access to own data
 */
public enum UserRole {
    ADMIN("Application Administrator", "Full system administration and configuration"),
    PHYSICIAN("Healthcare Professional", "Medical care, diagnoses, prescriptions, consultations"),
    CAREGIVER("Patient Caregiver", "Direct daily care, medication administration, observations"),
    FAMILY("Family Member / Legal Guardian", "Patient oversight, consent, monitoring"),
    PATIENT("Patient", "Limited access to own medical information");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if role has administrative privileges
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if role is a medical professional
     */
    public boolean isMedicalProfessional() {
        return this == PHYSICIAN;
    }

    /**
     * Check if role can provide direct care
     */
    public boolean isCareProvider() {
        return this == PHYSICIAN || this == CAREGIVER;
    }

    /**
     * Check if role has patient relationship
     */
    public boolean hasPatientRelationship() {
        return this == FAMILY || this == PATIENT;
    }

    /**
     * Get role hierarchy level (higher number = more privileges)
     */
    public int getHierarchyLevel() {
        switch (this) {
            case ADMIN: return 5;
            case PHYSICIAN: return 4;
            case CAREGIVER: return 3;
            case FAMILY: return 2;
            case PATIENT: return 1;
            default: return 0;
        }
    }

    /**
     * Check if this role has higher privileges than another role
     */
    public boolean hasHigherPrivilegesThan(UserRole other) {
        return this.getHierarchyLevel() > other.getHierarchyLevel();
    }
}
