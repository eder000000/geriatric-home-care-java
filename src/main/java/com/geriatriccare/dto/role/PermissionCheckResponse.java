package com.geriatriccare.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Check Response DTO
 * Result of permission check
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionCheckResponse {

    private String permission;
    private boolean hasPermission;
    private String userRole;
    private String message;

    public static PermissionCheckResponse allowed(String permission, String role) {
        return PermissionCheckResponse.builder()
                .permission(permission)
                .hasPermission(true)
                .userRole(role)
                .message("Permission granted")
                .build();
    }

    public static PermissionCheckResponse denied(String permission, String role) {
        return PermissionCheckResponse.builder()
                .permission(permission)
                .hasPermission(false)
                .userRole(role)
                .message("Permission denied")
                .build();
    }
}
