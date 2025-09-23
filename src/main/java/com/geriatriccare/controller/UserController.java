package com.geriatriccare.controller;

import com.geriatriccare.dto.PasswordChangeRequest;
import com.geriatriccare.dto.UserResponse;
import com.geriatriccare.dto.UserUpdateRequest;
import com.geriatriccare.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    // ========== PROFILE MANAGEMENT ==========
    
    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<UserResponse> getCurrentUser() {
        logger.info("Fetching current user profile");
        
        try {
            UserResponse response = userService.getCurrentUserProfile();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching current user profile", e);
            throw new RuntimeException("Failed to fetch user profile: " + e.getMessage());
        }
    }
    
    /**
     * Update current user profile
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        logger.info("Updating current user profile");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // Get current user ID from the authentication context
            UserResponse currentUser = userService.getCurrentUserProfile();
            
            UserResponse response = userService.updateProfile(currentUser.getId(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            throw new RuntimeException("Failed to update profile: " + e.getMessage());
        }
    }
    
    /**
     * Change current user password
     */
    @PutMapping("/me/password")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        logger.info("Changing password for current user");
        
        try {
            UserResponse currentUser = userService.getCurrentUserProfile();
            userService.changePassword(currentUser.getId(), request);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error changing password", e);
            throw new RuntimeException("Failed to change password: " + e.getMessage());
        }
    }
    
    // ========== USER MANAGEMENT (ADMIN FUNCTIONS) ==========
    
    /**
     * Get user by ID (Admin function)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        logger.info("Fetching user: {}", id);
        
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all active users (Admin function)
     */
    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Fetching all active users");
        
        List<UserResponse> users = userService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Update user profile (Admin function)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id, 
            @Valid @RequestBody UserUpdateRequest request) {
        
        logger.info("Admin updating user: {}", id);
        
        try {
            UserResponse response = userService.updateProfile(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error updating user: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Deactivate user account (Admin function)
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        logger.info("Deactivating user: {}", id);
        
        try {
            userService.deactivateUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deactivating user: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Reactivate user account (Admin function)
     */
    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> reactivateUser(@PathVariable UUID id) {
        logger.info("Reactivating user: {}", id);
        
        try {
            userService.reactivateUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error reactivating user: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // ========== UTILITY ENDPOINTS ==========
    
    /**
     * Check if user exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Boolean> userExists(@PathVariable UUID id) {
        logger.info("Checking if user exists: {}", id);
        
        boolean exists = userService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}
