package com.geriatriccare.dto.security;

public enum AuditEventType {
    // PHI Access Events
    PHI_VIEW("PHI Viewed", "Patient health information viewed"),
    PHI_EXPORT("PHI Exported", "Patient data exported from system"),
    PHI_PRINT("PHI Printed", "Patient record printed"),
    PHI_DOWNLOAD("PHI Downloaded", "Patient data downloaded"),
    
    // PHI Modification Events
    PHI_CREATE("PHI Created", "New patient record created"),
    PHI_UPDATE("PHI Updated", "Patient information updated"),
    PHI_DELETE("PHI Deleted", "Patient record deleted"),
    
    // Authentication Events
    USER_LOGIN("User Login", "User successfully logged in"),
    LOGIN_SUCCESS("Login Success", "User successfully logged in"), // Alias for backward compatibility
    USER_LOGOUT("User Logout", "User logged out"),
    LOGOUT("Logout", "User logged out"), // Alias for backward compatibility
    LOGIN_FAILED("Login Failed", "Failed login attempt"),
    LOGIN_FAILURE("Login Failure", "Failed login attempt"), // Alias for backward compatibility
    MFA_SETUP("MFA Setup", "Multi-factor authentication configured"),
    MFA_VERIFIED("MFA Verified", "MFA verification successful"),
    PASSWORD_CHANGED("Password Changed", "User password changed"),
    PASSWORD_RESET("Password Reset", "Password reset requested"),
    
    // Authorization Events
    ACCESS_DENIED("Access Denied", "Authorization denied"),
    UNAUTHORIZED_ACCESS("Unauthorized Access", "Unauthorized access attempt"), // Alias for backward compatibility
    PERMISSION_GRANTED("Permission Granted", "User permission granted"),
    PERMISSION_REVOKED("Permission Revoked", "User permission revoked"),
    ROLE_CHANGED("Role Changed", "User role modified"),
    
    // System Events
    SYSTEM_BACKUP("System Backup", "System backup performed"),
    SYSTEM_RESTORE("System Restore", "System restored from backup"),
    CONFIG_CHANGED("Configuration Changed", "System configuration modified"),
    DATABASE_MIGRATION("Database Migration", "Database schema migration"),
    
    // Security Events
    BREACH_ATTEMPT("Breach Attempt", "Potential security breach detected"),
    SUSPICIOUS_ACTIVITY("Suspicious Activity", "Unusual user behavior detected"),
    ACCOUNT_LOCKED("Account Locked", "User account locked"),
    ACCOUNT_UNLOCKED("Account Unlocked", "User account unlocked"),
    SESSION_EXPIRED("Session Expired", "User session expired"),
    SESSION_REVOKED("Session Revoked", "User session forcefully revoked"),
    
    // Compliance Events
    CONSENT_OBTAINED("Consent Obtained", "Patient consent documented"),
    CONSENT_REVOKED("Consent Revoked", "Patient consent withdrawn"),
    DATA_RETENTION_EXPIRED("Data Retention Expired", "Data retention period ended"),
    AUDIT_LOG_VIEWED("Audit Log Viewed", "Audit logs accessed"),
    COMPLIANCE_REPORT_GENERATED("Compliance Report Generated", "Compliance report created"),
    
    // AI/Clinical Events
    AI_RECOMMENDATION_GENERATED("AI Recommendation Generated", "AI system generated recommendation"),
    AI_CARE_PLAN_CREATED("AI Care Plan Created", "AI care plan generated"),
    MEDICATION_INTERACTION_CHECKED("Medication Interaction Checked", "Drug interaction screening performed");
    
    private final String displayName;
    private final String description;
    
    AuditEventType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    // Helper methods for HIPAAAuditService compatibility
    public boolean isPHIEvent() {
        return this == PHI_VIEW || this == PHI_EXPORT || this == PHI_PRINT || 
               this == PHI_DOWNLOAD || this == PHI_CREATE || this == PHI_UPDATE || 
               this == PHI_DELETE;
    }
    
    public boolean isSecurityCritical() {
        return this == BREACH_ATTEMPT || this == SUSPICIOUS_ACTIVITY || 
               this == LOGIN_FAILED || this == LOGIN_FAILURE || 
               this == ACCESS_DENIED || this == UNAUTHORIZED_ACCESS ||
               this == SESSION_REVOKED || this == ACCOUNT_LOCKED;
    }
}
