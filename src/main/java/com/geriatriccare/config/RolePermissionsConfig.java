package com.geriatriccare.config;

import com.geriatriccare.enums.Permission;
import com.geriatriccare.enums.UserRole;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Role-Permission Configuration
 * Maps roles to their allowed permissions
 * 
 * Based on Access Control Matrix from document requirements
 */
@Component
public class RolePermissionsConfig {

    private final Map<UserRole, Set<Permission>> rolePermissions;

    public RolePermissionsConfig() {
        rolePermissions = new HashMap<>();
        initializePermissions();
    }

    /**
     * Initialize role-permission mappings
     */
    private void initializePermissions() {
        
        // ========== ADMIN: Full System Access ==========
        Set<Permission> adminPermissions = new HashSet<>(Arrays.asList(Permission.values()));
        rolePermissions.put(UserRole.ADMIN, adminPermissions);

        // ========== PHYSICIAN: Medical Care & Diagnoses ==========
        rolePermissions.put(UserRole.PHYSICIAN, new HashSet<>(Arrays.asList(
            // Patient access
            Permission.PATIENT_READ,
            Permission.PATIENT_READ_PHI,
            Permission.PATIENT_WRITE,
            
            // Medication (full control)
            Permission.MEDICATION_READ,
            Permission.MEDICATION_WRITE,
            Permission.MEDICATION_DELETE,
            
            // Diagnosis (full control)
            Permission.DIAGNOSIS_READ,
            Permission.DIAGNOSIS_WRITE,
            Permission.DIAGNOSIS_DELETE,
            
            // Observations (clinical)
            Permission.OBSERVATION_READ,
            Permission.OBSERVATION_WRITE,
            Permission.OBSERVATION_CLINICAL,
            
            // Care plans
            Permission.CARE_PLAN_READ,
            Permission.CARE_PLAN_WRITE,
            Permission.CARE_PLAN_APPROVE,
            
            // Appointments
            Permission.APPOINTMENT_READ,
            Permission.APPOINTMENT_WRITE,
            Permission.APPOINTMENT_CANCEL,
            
            // Caregiver coordination
            Permission.CAREGIVER_READ,
            Permission.CAREGIVER_WRITE,
            Permission.CAREGIVER_TASK,
            
            // Reports
            Permission.REPORT_READ,
            Permission.REPORT_GENERATE,
            Permission.REPORT_EXPORT,
            
            // Audit (read-only)
            Permission.AUDIT_READ,
            
            // Consent
            Permission.CONSENT_READ,
            Permission.CONSENT_WRITE,
            Permission.CONSENT_APPROVE,
            
            // Emergency
            Permission.EMERGENCY_ACCESS,
            Permission.EMERGENCY_ACTIVATE,
            
            // AI
            Permission.AI_USE,
            Permission.AI_APPROVE
        )));

        // ========== CAREGIVER: Daily Care & Medication Administration ==========
        rolePermissions.put(UserRole.CAREGIVER, new HashSet<>(Arrays.asList(
            // Patient (limited)
            Permission.PATIENT_READ,
            
            // Medication (read + administer)
            Permission.MEDICATION_READ,
            Permission.MEDICATION_ADMINISTER,
            
            // Observations (non-clinical)
            Permission.OBSERVATION_READ,
            Permission.OBSERVATION_WRITE,
            Permission.OBSERVATION_NON_CLINICAL,
            
            // Care plan (read-only)
            Permission.CARE_PLAN_READ,
            
            // Appointments (read-only)
            Permission.APPOINTMENT_READ,
            
            // Caregiver tasks
            Permission.CAREGIVER_READ,
            Permission.CAREGIVER_TASK,
            
            // Reports (limited)
            Permission.REPORT_READ,
            
            // Emergency
            Permission.EMERGENCY_ACTIVATE,
            
            // AI (use only)
            Permission.AI_USE
        )));

        // ========== FAMILY: Patient Monitoring & Oversight ==========
        rolePermissions.put(UserRole.FAMILY, new HashSet<>(Arrays.asList(
            // Patient (summary only - no PHI details)
            Permission.PATIENT_READ,
            
            // Medication (read-only)
            Permission.MEDICATION_READ,
            
            // Observations (read-only, non-clinical)
            Permission.OBSERVATION_READ,
            
            // Care plan (read-only)
            Permission.CARE_PLAN_READ,
            
            // Appointments (read-only)
            Permission.APPOINTMENT_READ,
            
            // Caregiver (read-only)
            Permission.CAREGIVER_READ,
            
            // Reports (read-only)
            Permission.REPORT_READ,
            
            // Consent
            Permission.CONSENT_READ,
            Permission.CONSENT_WRITE
        )));

        // ========== PATIENT: Self-Access Only ==========
        rolePermissions.put(UserRole.PATIENT, new HashSet<>(Arrays.asList(
            // Own data (read-only)
            Permission.PATIENT_READ,
            Permission.PATIENT_READ_PHI,
            
            // Own medications (read-only)
            Permission.MEDICATION_READ,
            
            // Own diagnoses (read-only)
            Permission.DIAGNOSIS_READ,
            
            // Own observations (read-only)
            Permission.OBSERVATION_READ,
            
            // Own care plan (read-only)
            Permission.CARE_PLAN_READ,
            
            // Own appointments (read-only)
            Permission.APPOINTMENT_READ,
            
            // Own consent (read-only)
            Permission.CONSENT_READ
        )));
    }

    /**
     * Get permissions for a role
     * 
     * @param role User role
     * @return Set of permissions
     */
    public Set<Permission> getPermissionsForRole(UserRole role) {
        return new HashSet<>(rolePermissions.getOrDefault(role, Collections.emptySet()));
    }

    /**
     * Check if role has permission
     * 
     * @param role User role
     * @param permission Permission to check
     * @return true if role has permission
     */
    public boolean hasPermission(UserRole role, Permission permission) {
        Set<Permission> permissions = rolePermissions.get(role);
        return permissions != null && permissions.contains(permission);
    }

    /**
     * Get all permissions grouped by category
     * 
     * @return Map of category to permissions
     */
    public Map<String, List<Permission>> getPermissionsByCategory() {
        Map<String, List<Permission>> categorized = new HashMap<>();
        
        for (Permission permission : Permission.values()) {
            String category = permission.getCategory();
            categorized.computeIfAbsent(category, k -> new ArrayList<>()).add(permission);
        }
        
        return categorized;
    }

    /**
     * Get permission count by role
     * 
     * @return Map of role to permission count
     */
    public Map<UserRole, Integer> getPermissionCountByRole() {
        Map<UserRole, Integer> counts = new HashMap<>();
        
        for (UserRole role : UserRole.values()) {
            counts.put(role, rolePermissions.get(role).size());
        }
        
        return counts;
    }
}
