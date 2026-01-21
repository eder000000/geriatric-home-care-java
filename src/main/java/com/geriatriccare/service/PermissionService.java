package com.geriatriccare.service;

import com.geriatriccare.config.RolePermissionsConfig;
import com.geriatriccare.entity.User;
import com.geriatriccare.enums.Permission;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Permission Service
 * Handles permission checking and RBAC logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final RolePermissionsConfig rolePermissionsConfig;
    private final UserRepository userRepository;

    /**
     * Check if user has specific permission
     * 
     * @param user User entity
     * @param permission Permission to check
     * @return true if user has permission
     */
    public boolean hasPermission(User user, Permission permission) {
        if (user == null || permission == null) {
            return false;
        }

        // Admin has all permissions
        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }

        return rolePermissionsConfig.hasPermission(user.getRole(), permission);
    }

    /**
     * Check if current authenticated user has permission
     * 
     * @param permission Permission to check
     * @return true if current user has permission
     */
    public boolean hasPermission(Permission permission) {
        User currentUser = getCurrentUser();
        return hasPermission(currentUser, permission);
    }

    /**
     * Check if user has any of the specified permissions
     * 
     * @param user User entity
     * @param permissions Permissions to check
     * @return true if user has at least one permission
     */
    public boolean hasAnyPermission(User user, Permission... permissions) {
        for (Permission permission : permissions) {
            if (hasPermission(user, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has all specified permissions
     * 
     * @param user User entity
     * @param permissions Permissions to check
     * @return true if user has all permissions
     */
    public boolean hasAllPermissions(User user, Permission... permissions) {
        for (Permission permission : permissions) {
            if (!hasPermission(user, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if user can access patient data
     * 
     * @param user User entity
     * @param patientId Patient ID
     * @return true if user can access patient
     */
    public boolean canAccessPatient(User user, UUID patientId) {
        if (user == null || patientId == null) {
            return false;
        }

        // Check if user has patient read permission
        if (!hasPermission(user, Permission.PATIENT_READ)) {
            return false;
        }

        // PATIENT role can only access own data
        if (user.getRole() == UserRole.PATIENT) {
            // TODO: Check if user.getId() matches patient.userId
            // This requires linking User to Patient entity
            return false; // For now, deny all
        }

        // FAMILY role can only access related patients
        if (user.getRole() == UserRole.FAMILY) {
            // TODO: Check family-patient relationship
            // This requires FamilyMember entity from Sprint 10
            return false; // For now, deny all
        }

        // ADMIN, PHYSICIAN, CAREGIVER can access all patients (with proper permissions)
        return true;
    }

    /**
     * Check if user can modify medication
     * 
     * @param user User entity
     * @param medicationId Medication ID
     * @return true if user can modify medication
     */
    public boolean canModifyMedication(User user, UUID medicationId) {
        if (user == null || medicationId == null) {
            return false;
        }

        // Only PHYSICIAN can write/prescribe medications
        return user.getRole() == UserRole.PHYSICIAN && 
               hasPermission(user, Permission.MEDICATION_WRITE);
    }

    /**
     * Check if user can administer medication
     * 
     * @param user User entity
     * @return true if user can administer medication
     */
    public boolean canAdministerMedication(User user) {
        return hasPermission(user, Permission.MEDICATION_ADMINISTER);
    }

    /**
     * Get all permissions for role
     * 
     * @param role User role
     * @return Set of permissions
     */
    public Set<Permission> getPermissionsForRole(UserRole role) {
        return rolePermissionsConfig.getPermissionsForRole(role);
    }

    /**
     * Get permissions for current user
     * 
     * @return Set of permissions
     */
    public Set<Permission> getCurrentUserPermissions() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Collections.emptySet();
        }
        return getPermissionsForRole(currentUser.getRole());
    }

    /**
     * Get permissions grouped by category
     * 
     * @return Map of category to permissions
     */
    public Map<String, List<Permission>> getPermissionsByCategory() {
        return rolePermissionsConfig.getPermissionsByCategory();
    }

    /**
     * Check if permission requires audit logging
     * 
     * @param permission Permission to check
     * @return true if requires audit
     */
    public boolean requiresAudit(Permission permission) {
        return permission != null && permission.requiresAudit();
    }

    /**
     * Check if permission is PHI-related
     * 
     * @param permission Permission to check
     * @return true if PHI-related
     */
    public boolean isPhiRelated(Permission permission) {
        return permission != null && permission.isPhiRelated();
    }

    /**
     * Get current authenticated user
     * 
     * @return Current user or null
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String username = authentication.getName();
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return null;
        }
    }
}
