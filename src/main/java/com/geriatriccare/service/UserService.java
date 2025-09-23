package com.geriatriccare.service;

import com.geriatriccare.dto.PasswordChangeRequest;
import com.geriatriccare.dto.UserResponse;
import com.geriatriccare.dto.UserUpdateRequest;
import com.geriatriccare.entity.User;
import com.geriatriccare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // ========== PROFILE MANAGEMENT ==========
    
    /**
     * Update user profile information
     */
    public UserResponse updateProfile(UUID userId, UserUpdateRequest request) {
        logger.info("Updating profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email is already in use");
            }
        }
        
        // Update fields
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        
        User updated = userRepository.save(user);
        logger.info("Successfully updated profile for user: {}", userId);
        
        return convertToResponse(updated);
    }
    
    /**
     * Change user password
     */
    public void changePassword(UUID userId, PasswordChangeRequest request) {
        logger.info("Changing password for user: {}", userId);
        
        // Validate password confirmation
        if (!request.isNewPasswordConfirmed()) {
            throw new RuntimeException("New password and confirmation do not match");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        logger.info("Successfully changed password for user: {}", userId);
    }
    
    /**
     * Get current user profile
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        return convertToResponse(user);
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(UUID id) {
        logger.debug("Fetching user by ID: {}", id);
        
        return userRepository.findByIdAndIsActiveTrue(id)
                .map(this::convertToResponse);
    }
    
    // ========== USER MANAGEMENT (ADMIN FUNCTIONS) ==========
    
    /**
     * Get all active users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveUsers() {
        logger.debug("Fetching all active users");
        
        return userRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Deactivate user account
     */
    public void deactivateUser(UUID userId) {
        logger.info("Deactivating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        logger.info("Successfully deactivated user: {}", userId);
    }
    
    /**
     * Reactivate user account
     */
    public void reactivateUser(UUID userId) {
        logger.info("Reactivating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsActive(true);
        userRepository.save(user);
        
        logger.info("Successfully reactivated user: {}", userId);
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Check if user exists and is active
     */
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return userRepository.findByIdAndIsActiveTrue(id).isPresent();
    }
    
    // ========== CONVERSION METHODS ==========
    
    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        
        return response;
    }
}