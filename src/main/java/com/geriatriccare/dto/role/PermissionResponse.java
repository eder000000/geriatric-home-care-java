package com.geriatriccare.dto.role;

import com.geriatriccare.enums.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Response DTO
 * Information about a permission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {

    private String permission;
    private String displayName;
    private String description;
    private String category;
    private boolean isPhiRelated;
    private boolean requiresAudit;

    /**
     * Create from Permission
     */
    public static PermissionResponse from(Permission permission) {
        return PermissionResponse.builder()
                .permission(permission.name())
                .displayName(permission.getDisplayName())
                .description(permission.getDescription())
                .category(permission.getCategory())
                .isPhiRelated(permission.isPhiRelated())
                .requiresAudit(permission.requiresAudit())
                .build();
    }
}
