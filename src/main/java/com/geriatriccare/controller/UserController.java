package com.geriatriccare.controller;

import com.geriatriccare.dto.user.*;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.enums.UserStatus;
import com.geriatriccare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * User Management Controller
 * REST API for user management operations
 * 
 * Endpoints (9):
 * - POST /api/users - Create user (ADMIN only)
 * - GET /api/users - List users (ADMIN only)
 * - GET /api/users/{id} - Get user details
 * - PUT /api/users/{id} - Update user (ADMIN or self)
 * - DELETE /api/users/{id} - Deactivate user (ADMIN only)
 * - POST /api/users/{id}/activate - Activate user (ADMIN only)
 * - PUT /api/users/{id}/role - Change role (ADMIN only)
 * - POST /api/users/verify-email - Verify email
 * - GET /api/users/search - Search users
 * 
 * Additional endpoints:
 * - GET /api/users/me - Get current user profile
 * - PUT /api/users/me - Update current user profile
 * - PUT /api/users/me/password - Change password
 * - POST /api/users/{id}/resend-verification - Resend verification email
 * - GET /api/users/statistics - Get user statistics (ADMIN)
 * - GET /api/users/health - Health check
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User management operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Create new user (ADMIN only)
     * 
     * POST /api/users
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user", description = "Create a new user account (ADMIN only)")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {
        
        log.info("Creating user: {} by admin: {}", request.getUsername(), authentication.getName());
        
        UserResponse response = userService.createUser(request, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all users (ADMIN only)
     * 
     * GET /api/users?page=0&size=50&sortBy=lastName&sortDirection=ASC
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get paginated list of all users (ADMIN only)")
    public ResponseEntity<UserListResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        log.info("Getting all users - page: {}, size: {}", page, size);
        
        UserListResponse response = userService.getAllUsers(page, size, sortBy, sortDirection);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get users by role (ADMIN only)
     * 
     * GET /api/users/role/{role}?page=0&size=50
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "Get paginated list of users by role (ADMIN only)")
    public ResponseEntity<UserListResponse> getUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Getting users by role: {} - page: {}, size: {}", role, page, size);
        
        UserListResponse response = userService.getUsersByRole(role, page, size);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get users by status (ADMIN only)
     * 
     * GET /api/users/status/{status}?page=0&size=50
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by status", description = "Get paginated list of users by status (ADMIN only)")
    public ResponseEntity<UserListResponse> getUsersByStatus(
            @PathVariable UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Getting users by status: {} - page: {}, size: {}", status, page, size);
        
        UserListResponse response = userService.getUsersByStatus(status, page, size);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search users (ADMIN only)
     * 
     * GET /api/users/search?query=john&page=0&size=50
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users", description = "Search users by name, email, or username (ADMIN only)")
    public ResponseEntity<UserListResponse> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Searching users with query: {} - page: {}, size: {}", query, page, size);
        
        UserListResponse response = userService.searchUsers(query, page, size);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID
     * 
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER') or @userService.getCurrentUserId() == #id")
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.info("Getting user by ID: {}", id);
        
        UserResponse response = userService.getUserById(id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update user (ADMIN or self)
     * 
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getCurrentUserId() == #id")
    @Operation(summary = "Update user", description = "Update user information (ADMIN or self)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {
        
        log.info("Updating user: {} by: {}", id, authentication.getName());
        
        UserResponse response = userService.updateUser(id, request, authentication.getName());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate user (ADMIN only)
     * 
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivate user account (soft delete) (ADMIN only)")
    public ResponseEntity<Map<String, String>> deactivateUser(
            @PathVariable UUID id,
            Authentication authentication) {
        
        log.info("Deactivating user: {} by admin: {}", id, authentication.getName());
        
        userService.deactivateUser(id, authentication.getName());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deactivated successfully");
        response.put("userId", id.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Activate user (ADMIN only)
     * 
     * POST /api/users/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate user", description = "Activate user account (ADMIN only)")
    public ResponseEntity<Map<String, String>> activateUser(
            @PathVariable UUID id,
            Authentication authentication) {
        
        log.info("Activating user: {} by admin: {}", id, authentication.getName());
        
        userService.activateUser(id, authentication.getName());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User activated successfully");
        response.put("userId", id.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Change user role (ADMIN only)
     * 
     * PUT /api/users/{id}/role
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change user role", description = "Change user role (ADMIN only)")
    public ResponseEntity<Map<String, String>> assignRole(
            @PathVariable UUID id,
            @RequestParam UserRole role,
            Authentication authentication) {
        
        log.info("Changing role for user: {} to: {} by admin: {}", id, role, authentication.getName());
        
        userService.assignRole(id, role, authentication.getName());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Role assigned successfully");
        response.put("userId", id.toString());
        response.put("newRole", role.name());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verify email
     * 
     * POST /api/users/verify-email
     */
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with token")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) {
        
        log.info("Verifying email with token");
        
        userService.verifyEmail(request.getToken());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email verified successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Resend verification email
     * 
     * POST /api/users/{id}/resend-verification
     */
    @PostMapping("/{id}/resend-verification")
    @PreAuthorize("hasRole('ADMIN') or @userService.getCurrentUserId() == #id")
    @Operation(summary = "Resend verification email", description = "Resend verification email to user")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@PathVariable UUID id) {
        log.info("Resending verification email for user: {}", id);
        
        userService.resendVerificationEmail(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification email sent successfully");
        response.put("userId", id.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user profile
     * 
     * GET /api/users/me
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        log.info("Getting current user profile");
        
        UserResponse user = userService.getCurrentUser();
        UserProfileResponse profile = userService.getUserProfile(user.getId());
        
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user profile
     * 
     * PUT /api/users/me
     */
    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Update current user's profile")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileRequest request) {
        
        log.info("Updating current user profile");
        
        UUID userId = userService.getCurrentUserId();
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Change password
     * 
     * PUT /api/users/me/password
     */
    @PutMapping("/me/password")
    @Operation(summary = "Change password", description = "Change current user's password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        
        log.info("Changing password for current user");
        
        UUID userId = userService.getCurrentUserId();
        userService.changePassword(userId, request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user statistics (ADMIN only)
     * 
     * GET /api/users/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user statistics", description = "Get system-wide user statistics (ADMIN only)")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics() {
        log.info("Getting user statistics");
        
        UserStatisticsResponse response = userService.getUserStatistics();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     * 
     * GET /api/users/health
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "User service health check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "User Management Service");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
}
