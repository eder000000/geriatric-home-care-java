package com.geriatriccare.controller;

import com.geriatriccare.dto.role.*;
import com.geriatriccare.enums.Permission;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.service.PermissionService;
import com.geriatriccare.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Role Management Controller
 * REST API for role and permission management
 * 
 * Endpoints (5):
 * - GET /api/roles - List all roles (ADMIN only)
 * - GET /api/roles/{role}/permissions - Get permissions for role
 * - GET /api/permissions - List all permissions (ADMIN)
 * - POST /api/roles/check - Check if user has permission
 * - GET /api/roles/health - Health check
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role & Permission Management", description = "Role and permission operations")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final PermissionService permissionService;
    private final SecurityUtil securityUtil;

    /**
     * Get all roles (ADMIN only)
     * 
     * GET /api/roles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all roles", description = "Get list of all system roles with permission counts (ADMIN only)")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        log.info("Getting all roles");

        List<RoleResponse> roles = Arrays.stream(UserRole.values())
                .map(role -> {
                    Set<Permission> permissions = permissionService.getPermissionsForRole(role);
                    return RoleResponse.from(role, new ArrayList<>(permissions));
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(roles);
    }

    /**
     * Get permissions for specific role
     * 
     * GET /api/roles/{role}/permissions
     */
    @GetMapping("/{role}/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get role permissions", description = "Get all permissions for a specific role")
    public ResponseEntity<RoleResponse> getRolePermissions(@PathVariable String role) {
        log.info("Getting permissions for role: {}", role);

        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            Set<Permission> permissions = permissionService.getPermissionsForRole(userRole);
            
            RoleResponse response = RoleResponse.from(userRole, new ArrayList<>(permissions));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", role);
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Get all permissions (ADMIN only)
     * 
     * GET /api/permissions
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all permissions", description = "Get list of all system permissions (ADMIN only)")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        log.info("Getting all permissions");

        List<PermissionResponse> permissions = Arrays.stream(Permission.values())
                .map(PermissionResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(permissions);
    }

    /**
     * Get permissions grouped by category
     * 
     * GET /api/permissions/by-category
     */
    @GetMapping("/permissions/by-category")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get permissions by category", description = "Get permissions grouped by category (ADMIN only)")
    public ResponseEntity<Map<String, List<PermissionResponse>>> getPermissionsByCategory() {
        log.info("Getting permissions by category");

        Map<String, List<Permission>> categorized = permissionService.getPermissionsByCategory();
        
        Map<String, List<PermissionResponse>> response = categorized.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(PermissionResponse::from)
                                .collect(Collectors.toList())
                ));

        return ResponseEntity.ok(response);
    }

    /**
     * Check if current user has permission
     * 
     * POST /api/roles/check
     */
    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check permission", description = "Check if current user has specific permission")
    public ResponseEntity<PermissionCheckResponse> checkPermission(
            @Valid @RequestBody PermissionCheckRequest request) {
        
        log.info("Checking permission: {} for current user", request.getPermission());

        try {
            Permission permission = Permission.valueOf(request.getPermission().toUpperCase());
            boolean hasPermission = permissionService.hasPermission(permission);
            
            String userRole = securityUtil.getCurrentUser() != null ? 
                    securityUtil.getCurrentUser().getRole().name() : "UNKNOWN";

            PermissionCheckResponse response = hasPermission ?
                    PermissionCheckResponse.allowed(permission.name(), userRole) :
                    PermissionCheckResponse.denied(permission.name(), userRole);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid permission: {}", request.getPermission());
            throw new IllegalArgumentException("Invalid permission: " + request.getPermission());
        }
    }

    /**
     * Get current user's permissions
     * 
     * GET /api/roles/me/permissions
     */
    @GetMapping("/me/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my permissions", description = "Get current user's permissions")
    public ResponseEntity<List<PermissionResponse>> getMyPermissions() {
        log.info("Getting permissions for current user");

        Set<Permission> permissions = permissionService.getCurrentUserPermissions();
        
        List<PermissionResponse> response = permissions.stream()
                .map(PermissionResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get complete role-permission matrix (ADMIN only)
     * 
     * GET /api/roles/matrix
     */
    @GetMapping("/matrix")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get role-permission matrix", description = "Get complete role-permission mapping (ADMIN only)")
    public ResponseEntity<RolePermissionsResponse> getRolePermissionMatrix() {
        log.info("Getting role-permission matrix");

        Map<String, List<String>> rolePermissions = new HashMap<>();
        
        for (UserRole role : UserRole.values()) {
            Set<Permission> permissions = permissionService.getPermissionsForRole(role);
            List<String> permissionNames = permissions.stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            rolePermissions.put(role.name(), permissionNames);
        }

        Map<String, List<Permission>> permissionsByCategory = 
                permissionService.getPermissionsByCategory();

        RolePermissionsResponse response = RolePermissionsResponse.create(
                rolePermissions, permissionsByCategory);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     * 
     * GET /api/roles/health
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Role service health check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Role & Permission Service");
        health.put("totalRoles", UserRole.values().length);
        health.put("totalPermissions", Permission.values().length);
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
}
