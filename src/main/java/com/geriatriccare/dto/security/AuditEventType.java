package com.geriatriccare.dto.security;

public enum AuditEventType {
    // Authentication Events
    LOGIN_SUCCESS("Login Success", "User successfully logged in", "AUTHENTICATION"),
    LOGIN_FAILURE("Login Failure", "Failed login attempt", "AUTHENTICATION"),
    LOGOUT("Logout", "User logged out", "AUTHENTICATION"),
    MFA_SETUP("MFA Setup", "Multi-factor authentication configured", "AUTHENTICATION"),
    MFA_VERIFICATION("MFA Verification", "MFA code verification attempt", "AUTHENTICATION"),
    PASSWORD_CHANGE("Password Change", "User password changed", "AUTHENTICATION"),
    PASSWORD_RESET("Password Reset", "Password reset via token", "AUTHENTICATION"),
    
    // PHI Access Events (HIPAA Critical)
    PHI_VIEW("PHI Viewed", "Protected Health Information viewed", "PHI_ACCESS"),
    PHI_CREATE("PHI Created", "Protected Health Information created", "PHI_ACCESS"),
    PHI_UPDATE("PHI Updated", "Protected Health Information updated", "PHI_ACCESS"),
    PHI_DELETE("PHI Deleted", "Protected Health Information deleted", "PHI_ACCESS"),
    PHI_EXPORT("PHI Exported", "Protected Health Information exported", "PHI_ACCESS"),
    PHI_PRINT("PHI Printed", "Protected Health Information printed", "PHI_ACCESS"),
    
    // Data Access Events
    PATIENT_VIEW("Patient Viewed", "Patient record accessed", "DATA_ACCESS"),
    PATIENT_CREATE("Patient Created", "New patient record created", "DATA_ACCESS"),
    PATIENT_UPDATE("Patient Updated", "Patient record modified", "DATA_ACCESS"),
    PATIENT_DELETE("Patient Deleted", "Patient record deleted", "DATA_ACCESS"),
    
    // Administrative Events
    USER_CREATE("User Created", "New user account created", "ADMINISTRATION"),
    USER_UPDATE("User Updated", "User account modified", "ADMINISTRATION"),
    USER_DELETE("User Deleted", "User account deleted", "ADMINISTRATION"),
    USER_ROLE_CHANGE("Role Changed", "User role/permissions changed", "ADMINISTRATION"),
    ACCOUNT_LOCKED("Account Locked", "User account locked due to failed attempts", "ADMINISTRATION"),
    ACCOUNT_UNLOCKED("Account Unlocked", "User account unlocked by admin", "ADMINISTRATION"),
    
    // Security Events
    UNAUTHORIZED_ACCESS("Unauthorized Access", "Attempted access to restricted resource", "SECURITY"),
    SESSION_EXPIRED("Session Expired", "User session expired", "SECURITY"),
    SESSION_REVOKED("Session Revoked", "User session revoked", "SECURITY"),
    SUSPICIOUS_ACTIVITY("Suspicious Activity", "Suspicious behavior detected", "SECURITY"),
    
    // System Events
    SYSTEM_CONFIG_CHANGE("Config Changed", "System configuration modified", "SYSTEM"),
    BACKUP_CREATED("Backup Created", "System backup performed", "SYSTEM"),
    BACKUP_RESTORED("Backup Restored", "System restored from backup", "SYSTEM"),
    
    // AI Events
    AI_RECOMMENDATION("AI Recommendation", "AI-generated recommendation", "AI_OPERATION"),
    AI_REVIEW("AI Review", "AI recommendation reviewed", "AI_OPERATION");
    
    private final String displayName;
    private final String description;
    private final String category;
    
    AuditEventType(String displayName, String description, String category) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    
    public boolean isPHIEvent() {
        return category.equals("PHI_ACCESS");
    }
    
    public boolean isSecurityCritical() {
        return category.equals("SECURITY") || category.equals("PHI_ACCESS");
    }
}
