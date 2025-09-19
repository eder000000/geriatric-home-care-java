package com.geriatriccare.entity;

public enum UserRole {
    OWNER("Owner"),
    ADMIN("Administrator"),
    CAREGIVER("Caregiver"),
    FAMILY("Family Member"),
    PATIENT("Patient");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Get role hierarchy level (higher number = more privileges)
     */
    public int getHierarchyLevel() {
        switch (this) {
            case OWNER: return 5;
            case ADMIN: return 4;
            case CAREGIVER: return 3;
            case FAMILY: return 2;
            case PATIENT: return 1;
            default: return 0;
        }
    }
    
    /**
     * Check if this role has higher or equal privileges than another role
     */
    public boolean hasPrivilegeOver(UserRole otherRole) {
        return this.getHierarchyLevel() >= otherRole.getHierarchyLevel();
    }
}