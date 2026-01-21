package com.geriatriccare.dto.role;

import com.geriatriccare.enums.Permission;
import com.geriatriccare.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Role Permissions Response DTO
 * Complete role-permission matrix
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionsResponse {

    private Map<String, List<String>> rolePermissions;
    private Map<String, List<PermissionResponse>> permissionsByCategory;
    private int totalRoles;
    private int totalPermissions;

    /**
     * Create comprehensive response
     */
    public static RolePermissionsResponse create(
            Map<String, List<String>> rolePermissions,
            Map<String, List<Permission>> permissionsByCategory) {
        
        Map<String, List<PermissionResponse>> categorized = permissionsByCategory.entrySet()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(PermissionResponse::from)
                                .toList()
                ));

        return RolePermissionsResponse.builder()
                .rolePermissions(rolePermissions)
                .permissionsByCategory(categorized)
                .totalRoles(UserRole.values().length)
                .totalPermissions(Permission.values().length)
                .build();
    }
}
