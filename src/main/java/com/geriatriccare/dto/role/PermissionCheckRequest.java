package com.geriatriccare.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Check Request DTO
 * Check if current user has specific permission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCheckRequest {

    @NotBlank(message = "Permission name is required")
    private String permission;
}
