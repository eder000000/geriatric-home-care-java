package com.geriatriccare.enums;

/**
 * Permission Enumeration
 * Defines granular permissions for the system
 * 
 * Permission Categories:
 * - PATIENT: Patient data access
 * - MEDICATION: Medication management
 * - DIAGNOSIS: Diagnosis and medical records
 * - OBSERVATION: Clinical and non-clinical observations
 * - USER: User management
 * - REPORT: Reporting and analytics
 * - AUDIT: Audit log access
 * - CAREGIVER: Caregiver management
 * - APPOINTMENT: Appointment scheduling
 * - CONSENT: Consent and guardianship
 */
public enum Permission {
    
    // ========== PATIENT PERMISSIONS ==========
    PATIENT_READ("Read patient information", "View patient demographics and basic info"),
    PATIENT_READ_PHI("Read PHI", "View protected health information"),
    PATIENT_WRITE("Write patient data", "Create and update patient information"),
    PATIENT_DELETE("Delete patient", "Remove patient records (soft delete)"),
    
    // ========== MEDICATION PERMISSIONS ==========
    MEDICATION_READ("Read medications", "View medication lists and schedules"),
    MEDICATION_WRITE("Prescribe medications", "Create and update medication prescriptions"),
    MEDICATION_ADMINISTER("Administer medications", "Record medication administration"),
    MEDICATION_DELETE("Delete medications", "Remove medication records"),
    
    // ========== DIAGNOSIS PERMISSIONS ==========
    DIAGNOSIS_READ("Read diagnoses", "View patient diagnoses"),
    DIAGNOSIS_WRITE("Write diagnoses", "Add and update diagnoses"),
    DIAGNOSIS_DELETE("Delete diagnoses", "Remove diagnosis records"),
    
    // ========== OBSERVATION PERMISSIONS ==========
    OBSERVATION_READ("Read observations", "View clinical and non-clinical observations"),
    OBSERVATION_WRITE("Write observations", "Record observations"),
    OBSERVATION_CLINICAL("Clinical observations", "Record clinical observations (vitals, symptoms)"),
    OBSERVATION_NON_CLINICAL("Non-clinical observations", "Record daily care observations"),
    
    // ========== CARE PLAN PERMISSIONS ==========
    CARE_PLAN_READ("Read care plans", "View patient care plans"),
    CARE_PLAN_WRITE("Write care plans", "Create and update care plans"),
    CARE_PLAN_APPROVE("Approve care plans", "Approve and activate care plans"),
    
    // ========== APPOINTMENT PERMISSIONS ==========
    APPOINTMENT_READ("Read appointments", "View appointment schedules"),
    APPOINTMENT_WRITE("Schedule appointments", "Create and modify appointments"),
    APPOINTMENT_CANCEL("Cancel appointments", "Cancel scheduled appointments"),
    
    // ========== USER MANAGEMENT PERMISSIONS ==========
    USER_READ("Read users", "View user information"),
    USER_WRITE("Manage users", "Create and update user accounts"),
    USER_DELETE("Delete users", "Deactivate user accounts"),
    USER_ASSIGN_ROLE("Assign roles", "Change user roles"),
    
    // ========== CAREGIVER PERMISSIONS ==========
    CAREGIVER_READ("Read caregiver info", "View caregiver assignments and schedules"),
    CAREGIVER_WRITE("Manage caregivers", "Assign caregivers and manage schedules"),
    CAREGIVER_TASK("Manage tasks", "Create and assign caregiver tasks"),
    
    // ========== REPORT PERMISSIONS ==========
    REPORT_READ("Read reports", "View generated reports"),
    REPORT_GENERATE("Generate reports", "Create new reports"),
    REPORT_EXPORT("Export reports", "Export reports to various formats"),
    
    // ========== AUDIT PERMISSIONS ==========
    AUDIT_READ("Read audit logs", "View audit trail and system logs"),
    AUDIT_EXPORT("Export audit logs", "Export audit logs"),
    
    // ========== CONSENT PERMISSIONS ==========
    CONSENT_READ("Read consent", "View consent and guardianship records"),
    CONSENT_WRITE("Manage consent", "Create and update consent forms"),
    CONSENT_APPROVE("Approve consent", "Approve legal consent documents"),
    
    // ========== EMERGENCY PERMISSIONS ==========
    EMERGENCY_ACCESS("Emergency access", "Override normal restrictions in emergency"),
    EMERGENCY_ACTIVATE("Activate emergency", "Trigger emergency protocols"),
    
    // ========== SYSTEM PERMISSIONS ==========
    SYSTEM_CONFIG("System configuration", "Modify system settings"),
    CATALOG_MANAGE("Manage catalogs", "Manage medical catalogs (ICD-10, etc.)"),
    
    // ========== AI PERMISSIONS ==========
    AI_USE("Use AI features", "Access AI recommendations and tools"),
    AI_APPROVE("Approve AI recommendations", "Approve AI-generated recommendations");

    private final String displayName;
    private final String description;

    Permission(String displayName, String description) {
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
     * Get permission category
     */
    public String getCategory() {
        String name = this.name();
        int underscoreIndex = name.indexOf('_');
        return underscoreIndex > 0 ? name.substring(0, underscoreIndex) : "OTHER";
    }

    /**
     * Check if permission is related to PHI
     */
    public boolean isPhiRelated() {
        return this == PATIENT_READ_PHI || 
               this.name().startsWith("DIAGNOSIS") ||
               this.name().startsWith("MEDICATION") ||
               this == OBSERVATION_CLINICAL;
    }

    /**
     * Check if permission requires audit logging
     */
    public boolean requiresAudit() {
        return this.name().contains("WRITE") || 
               this.name().contains("DELETE") ||
               this.name().contains("APPROVE") ||
               this.isPhiRelated();
    }
}
