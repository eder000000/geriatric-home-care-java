package com.geriatriccare.dto.role;

import com.geriatriccare.enums.Permission;
import com.geriatriccare.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Role Response DTO
 * Information about a user role
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {

    private String role;
    private String displayName;
    private String description;
    private int hierarchyLevel;
    private int permissionCount;
    private List<String> permissions;

    /**
     * Create from UserRole
     */
    public static RoleResponse from(UserRole role, List<Permission> permissions) {
        return RoleResponse.builder()
                .role(role.name())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .hierarchyLevel(role.getHierarchyLevel())
                .permissionCount(permissions.size())
                .permissions(permissions.stream()
                        .map(Enum::name)
                        .toList())
                .build();
    }
}
