package com.geriatriccare.util;

import com.geriatriccare.entity.User;
import com.geriatriccare.enums.Permission;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Security Utility
 * Helper methods for security checks in @PreAuthorize expressions
 * 
 * Usage in controllers:
 * @PreAuthorize("@securityUtil.hasPermission('PATIENT_READ')")
 * @PreAuthorize("@securityUtil.canAccessPatient(#patientId)")
 */
@Component("securityUtil")
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    /**
     * Get current authenticated user
     * 
     * @return Current user or null
     */
    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String username = authentication.getName();
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get current user ID
     * 
     * @return Current user ID or null
     */
    public UUID getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Check if current user has role
     * 
     * @param role Role to check
     * @return true if user has role
     */
    public boolean hasRole(String role) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }

        try {
            UserRole userRole = UserRole.valueOf(role);
            return user.getRole() == userRole;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if current user has any of the roles
     * 
     * @param roles Roles to check
     * @return true if user has any role
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has permission
     * 
     * @param permissionName Permission name
     * @return true if user has permission
     */
    public boolean hasPermission(String permissionName) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }

        try {
            Permission permission = Permission.valueOf(permissionName);
            return permissionService.hasPermission(user, permission);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if current user can access patient
     * 
     * @param patientId Patient ID
     * @return true if user can access
     */
    public boolean canAccessPatient(UUID patientId) {
        User user = getCurrentUser();
        return permissionService.canAccessPatient(user, patientId);
    }

    /**
     * Check if current user can modify medication
     * 
     * @param medicationId Medication ID
     * @return true if user can modify
     */
    public boolean canModifyMedication(UUID medicationId) {
        User user = getCurrentUser();
        return permissionService.canModifyMedication(user, medicationId);
    }

    /**
     * Check if current user is admin
     * 
     * @return true if admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is physician
     * 
     * @return true if physician
     */
    public boolean isPhysician() {
        return hasRole("PHYSICIAN");
    }

    /**
     * Check if current user is caregiver
     * 
     * @return true if caregiver
     */
    public boolean isCaregiver() {
        return hasRole("CAREGIVER");
    }
}
