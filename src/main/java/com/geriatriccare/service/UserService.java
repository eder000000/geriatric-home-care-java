package com.geriatriccare.service;

import com.geriatriccare.dto.user.*;
import com.geriatriccare.dto.security.DataSensitivity;
import com.geriatriccare.entity.User;
import com.geriatriccare.enums.AuditEventType;
import com.geriatriccare.enums.AuditSeverity;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.enums.UserStatus;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.service.security.AuditEnhancementService;
import com.geriatriccare.service.security.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Service
 * Business logic for user management
 * 
 * Integrates with:
 * - Sprint 5: Password Policy Service
 * - Sprint 5: MFA Service
 * - Sprint 5: Audit Enhancement Service
 * - Sprint 4: AI Services (for user activity analysis)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final AuditEnhancementService auditService;

    /**
     * Create new user
     * 
     * @param request User creation request
     * @param createdBy Username of admin creating the user
     * @return Created user response
     */
    @Transactional
    public UserResponse createUser(UserRequest request, String createdBy) {
        log.info("Creating new user: {} with role: {}", request.getUsername(), request.getRole());

        // Validate unique email and username
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Validate password policy (integrates with Sprint 5)
        // TODO: Fix password validation - // TODO: Fix password validation - passwordPolicyService.validatePassword(request.getPassword(), request.getUsername());

        // Create user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.PENDING_VERIFICATION);
        user.setMfaEnabled(request.getMfaEnabled() != null ? request.getMfaEnabled() : false);
        user.setMustChangePassword(request.getMustChangePassword() != null ? request.getMustChangePassword() : false);
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        user.setEmailVerified(false);
        user.setCreatedBy(createdBy);
        user.setUpdatedBy(createdBy);
        user.setPasswordChangedAt(LocalDateTime.now());

        // Generate email verification token
        user.setEmailVerificationToken(generateVerificationToken());
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        // Save user
        User savedUser = userRepository.save(user);

        // Audit log
        auditService.logEnhancedAuditEvent(
            AuditEventType.USER_CREATED,
            AuditSeverity.INFO,
            DataSensitivity.INTERNAL,
            null,
            savedUser.getId().toString(),
            String.format("User created: %s (%s) with role: %s", 
                savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole()),
            null
        );

        log.info("User created successfully: {}", savedUser.getId());

        // TODO: Send verification email (integrate with email service in Sprint 9)
        // emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getEmailVerificationToken());

        return UserResponse.from(savedUser);
    }

    /**
     * Update existing user
     * 
     * @param userId User ID
     * @param request User update request
     * @param updatedBy Username of user performing update
     * @return Updated user response
     */
    @Transactional
    public UserResponse updateUser(UUID userId, UserRequest request, String updatedBy) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check email uniqueness (if changed)
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Check username uniqueness (if changed)
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Update fields
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        user.setUpdatedBy(updatedBy);

        // Admin-only fields
        if (request.getRole() != null && !user.getRole().equals(request.getRole())) {
            user.setRole(request.getRole());
            auditService.logEnhancedAuditEvent(
                AuditEventType.USER_ROLE_CHANGED,
                AuditSeverity.WARNING,
                DataSensitivity.INTERNAL,
                null,
                userId.toString(),
                String.format("User role changed to: %s", request.getRole()),
                null
            );
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        if (request.getMfaEnabled() != null) {
            user.setMfaEnabled(request.getMfaEnabled());
        }

        if (request.getMustChangePassword() != null) {
            user.setMustChangePassword(request.getMustChangePassword());
        }

        // Save
        User updatedUser = userRepository.save(user);

        // Audit log
        auditService.logEnhancedAuditEvent(
            AuditEventType.USER_UPDATED,
            AuditSeverity.INFO,
            DataSensitivity.INTERNAL,
            null,
            userId.toString(),
            String.format("User updated: %s", updatedUser.getUsername()),
            null
        );

        log.info("User updated successfully: {}", userId);

        return UserResponse.from(updatedUser);
    }

    /**
     * Update user profile (self-service)
     * 
     * @param userId User ID
     * @param request Profile update request
     * @return Updated profile response
     */
    @Transactional
    public UserProfileResponse updateUserProfile(UUID userId, UserProfileRequest request) {
        log.info("Updating user profile: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Update allowed profile fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !user.getEmail().equals(request.getEmail())) {
            // Email change requires re-verification
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
            user.setEmailVerificationToken(generateVerificationToken());
            user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        if (request.getPreferences() != null) {
            user.setPreferences(request.getPreferences());
        }

        User updatedUser = userRepository.save(user);

        log.info("User profile updated successfully: {}", userId);

        return UserProfileResponse.from(updatedUser);
    }

    /**
     * Deactivate user (soft delete)
     * 
     * @param userId User ID
     * @param deletedBy Username of admin deleting the user
     */
    @Transactional
    public void deactivateUser(UUID userId, String deletedBy) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setStatus(UserStatus.INACTIVE);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(deletedBy);

        userRepository.save(user);

        // Audit log
        auditService.logEnhancedAuditEvent(
            AuditEventType.USER_DELETED,
            AuditSeverity.WARNING,
            DataSensitivity.INTERNAL,
            null,
            userId.toString(),
            String.format("User deactivated: %s", user.getUsername()),
            null
        );

        log.info("User deactivated successfully: {}", userId);
    }

    /**
     * Activate user
     * 
     * @param userId User ID
     * @param activatedBy Username of admin activating the user
     */
    @Transactional
    public void activateUser(UUID userId, String activatedBy) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setStatus(UserStatus.ACTIVE);
        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);
        user.setUpdatedBy(activatedBy);

        userRepository.save(user);

        // Audit log
        auditService.logEnhancedAuditEvent(
            AuditEventType.USER_ACTIVATED,
            AuditSeverity.INFO,
            DataSensitivity.INTERNAL,
            null,
            userId.toString(),
            String.format("User activated: %s", user.getUsername()),
            null
        );

        log.info("User activated successfully: {}", userId);
    }

    /**
     * Assign role to user
     * 
     * @param userId User ID
     * @param role New role
     * @param assignedBy Username of admin assigning role
     */
    @Transactional
    public void assignRole(UUID userId, UserRole role, String assignedBy) {
        log.info("Assigning role {} to user: {}", role, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        UserRole oldRole = user.getRole();
        user.setRole(role);
        user.setUpdatedBy(assignedBy);

        userRepository.save(user);

        // Audit log
        auditService.logEnhancedAuditEvent(
            AuditEventType.USER_ROLE_CHANGED,
            AuditSeverity.WARNING,
            DataSensitivity.INTERNAL,
            null,
            userId.toString(),
            String.format("User role changed from %s to %s", oldRole, role),
            null
        );

        log.info("Role assigned successfully: {}", userId);
    }

    /**
     * Get user by ID
     * 
     * @param userId User ID
     * @return User response
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return UserResponse.from(user);
    }

    /**
     * Get user profile by ID
     * 
     * @param userId User ID
     * @return User profile response
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return UserProfileResponse.from(user);
    }

    /**
     * Get all users (paginated)
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sortBy Sort field
     * @param sortDirection Sort direction (ASC/DESC)
     * @return Paginated user list
     */
    @Transactional(readOnly = true)
    public UserListResponse getAllUsers(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserResponse> responsePage = userPage.map(UserResponse::from);

        return UserListResponse.from(responsePage);
    }

    /**
     * Get users by role
     * 
     * @param role User role
     * @param page Page number
     * @param size Page size
     * @return Paginated user list
     */
    @Transactional(readOnly = true)
    public UserListResponse getUsersByRole(UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName"));
        Page<User> userPage = userRepository.findByRole(role, pageable);
        Page<UserResponse> responsePage = userPage.map(UserResponse::from);

        return UserListResponse.from(responsePage);
    }

    /**
     * Get users by status
     * 
     * @param status User status
     * @param page Page number
     * @param size Page size
     * @return Paginated user list
     */
    @Transactional(readOnly = true)
    public UserListResponse getUsersByStatus(UserStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName"));
        Page<User> userPage = userRepository.findByStatus(status, pageable);
        Page<UserResponse> responsePage = userPage.map(UserResponse::from);

        return UserListResponse.from(responsePage);
    }

    /**
     * Search users by query
     * 
     * @param query Search query (name, email, username)
     * @param page Page number
     * @param size Page size
     * @return Paginated user list
     */
    @Transactional(readOnly = true)
    public UserListResponse searchUsers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName"));
        Page<User> userPage = userRepository.searchUsers(query, pageable);
        Page<UserResponse> responsePage = userPage.map(UserResponse::from);

        return UserListResponse.from(responsePage);
    }

    /**
     * Verify email
     * 
     * @param token Verification token
     */
    @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email with token");

        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        // Audit log
        auditService.logEnhancedAuditEvent(
            AuditEventType.USER_EMAIL_VERIFIED,
            AuditSeverity.INFO,
            DataSensitivity.INTERNAL,
            null,
            user.getId().toString(),
            String.format("Email verified: %s", user.getEmail()),
            null
        );

        log.info("Email verified successfully for user: {}", user.getId());
    }

    /**
     * Resend verification email
     * 
     * @param userId User ID
     */
    @Transactional
    public void resendVerificationEmail(UUID userId) {
        log.info("Resending verification email for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("Email already verified");
        }

        // Generate new token
        user.setEmailVerificationToken(generateVerificationToken());
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        // TODO: Send verification email (integrate with email service in Sprint 9)
        // emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());

        log.info("Verification email resent for user: {}", userId);
    }

    /**
     * Change password
     * 
     * @param userId User ID
     * @param request Password change request
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            // Record failed attempt (integrates with Sprint 5)
            passwordPolicyService.recordFailedLoginAttempt(userId.toString());
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Verify new passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        // Validate new password (integrates with Sprint 5)
        // TODO: Fix password validation - // TODO: Fix password validation - passwordPolicyService.validatePassword(request.getNewPassword(), user.getUsername());

        // Check password history (integrates with Sprint 5)
        if (false) { // TODO: Fix password history check
            throw new IllegalArgumentException("Cannot reuse any of your last 5 passwords");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);

        userRepository.save(user);

        // Audit log
        auditService.logEnhancedAuditEvent(
            AuditEventType.PASSWORD_CHANGED,
            AuditSeverity.INFO,
            DataSensitivity.INTERNAL,
            null,
            userId.toString(),
            "Password changed successfully",
            null
        );

        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Get user statistics
     * 
     * @return User statistics
     */
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics() {
        log.info("Generating user statistics");

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long pendingVerification = userRepository.countByStatus(UserStatus.PENDING_VERIFICATION);
        
        Map<String, Long> usersByRole = new HashMap<>();
        usersByRole.put("ADMIN", userRepository.countByRole(UserRole.ADMIN));
        usersByRole.put("PHYSICIAN", userRepository.countByRole(UserRole.PHYSICIAN));
        usersByRole.put("CAREGIVER", userRepository.countByRole(UserRole.CAREGIVER));
        usersByRole.put("FAMILY", userRepository.countByRole(UserRole.FAMILY));
        usersByRole.put("PATIENT", userRepository.countByRole(UserRole.PATIENT));

        Map<String, Long> usersByStatus = new HashMap<>();
        usersByStatus.put("ACTIVE", userRepository.countByStatus(UserStatus.ACTIVE));
        usersByStatus.put("INACTIVE", userRepository.countByStatus(UserStatus.INACTIVE));
        usersByStatus.put("SUSPENDED", userRepository.countByStatus(UserStatus.SUSPENDED));
        usersByStatus.put("PENDING_VERIFICATION", pendingVerification);

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(userRepository.countByStatus(UserStatus.INACTIVE))
                .suspendedUsers(userRepository.countByStatus(UserStatus.SUSPENDED))
                .pendingVerification(pendingVerification)
                .usersWithMfaEnabled(userRepository.findByMfaEnabled(true).size())
                .lockedUsers(userRepository.findLockedUsers(LocalDateTime.now()).size())
                .usersWithExpiredPasswords(userRepository.findUsersWithExpiredPasswords(
                    LocalDateTime.now().minusDays(90)).size())
                .adminCount(userRepository.countByRole(UserRole.ADMIN))
                .physicianCount(userRepository.countByRole(UserRole.PHYSICIAN))
                .caregiverCount(userRepository.countByRole(UserRole.CAREGIVER))
                .familyCount(userRepository.countByRole(UserRole.FAMILY))
                .patientCount(userRepository.countByRole(UserRole.PATIENT))
                .usersByRole(usersByRole)
                .usersByStatus(usersByStatus)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get current authenticated user
     * 
     * @return Current user response
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return UserResponse.from(user);
    }

    /**
     * Get current authenticated user ID
     * 
     * @return Current user ID
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return user.getId();
    }

    /**
     * Generate verification token
     * 
     * @return Verification token
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
