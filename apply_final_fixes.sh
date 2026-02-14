#!/bin/bash

echo "🔧 Applying final fixes..."

# Fix 1: PasswordPolicyService - use setIsValid instead of setValid
cat > src/main/java/com/geriatriccare/service/security/PasswordPolicyService.java << 'EOF'
package com.geriatriccare.service.security;

import com.geriatriccare.dto.security.PasswordValidationRequest;
import com.geriatriccare.dto.security.PasswordValidationResponse;
import com.geriatriccare.entity.PasswordHistory;
import com.geriatriccare.entity.User;
import com.geriatriccare.exception.InvalidPasswordException;
import com.geriatriccare.repository.PasswordHistoryRepository;
import com.geriatriccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PasswordPolicyService {
    
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int PASSWORD_HISTORY_SIZE = 5;
    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;
    
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_DIGIT = "0123456789";
    private static final String CHAR_SPECIAL = "!@#$%^&*(),.?\":{}|<>";
    private static final SecureRandom random = new SecureRandom();

    @Autowired private PasswordHistoryRepository passwordHistoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public void validatePassword(String password, String username) {
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidPasswordException("Password cannot be empty");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (!UPPERCASE.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain uppercase letter");
        }
        if (!LOWERCASE.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain lowercase letter");
        }
        if (!DIGIT.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain digit");
        }
        if (!SPECIAL.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain special character");
        }
        if (username != null && password.equalsIgnoreCase(username)) {
            throw new InvalidPasswordException("Password cannot be same as username");
        }
    }

    public PasswordValidationResponse validatePassword(PasswordValidationRequest request) {
        PasswordValidationResponse response = new PasswordValidationResponse();
        response.setIsValid(true);
        
        try {
            validatePassword(request.getPassword(), request.getUsername());
            response.setMessage("Password meets all requirements");
        } catch (InvalidPasswordException e) {
            response.setIsValid(false);
            response.setMessage(e.getMessage());
        }
        
        return response;
    }

    public boolean isPasswordInHistory(UUID userId, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository.findByUserIdOrderByChangedAtDesc(userId);
        int checkCount = Math.min(PASSWORD_HISTORY_SIZE, history.size());
        for (int i = 0; i < checkCount; i++) {
            if (passwordEncoder.matches(newPassword, history.get(i).getPasswordHash())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void addToPasswordHistory(UUID userId, String passwordHash) {
        PasswordHistory history = new PasswordHistory();
        history.setUserId(userId);
        history.setPasswordHash(passwordHash);
        history.setChangedAt(LocalDateTime.now());
        passwordHistoryRepository.save(history);

        List<PasswordHistory> allHistory = passwordHistoryRepository.findByUserIdOrderByChangedAtDesc(userId);
        if (allHistory.size() > PASSWORD_HISTORY_SIZE) {
            passwordHistoryRepository.deleteAll(allHistory.subList(PASSWORD_HISTORY_SIZE, allHistory.size()));
        }
    }

    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        UUID id = UUID.fromString(userId);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            incrementFailedLoginAttempts(user);
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        validatePassword(newPassword, user.getUsername());
        
        if (isPasswordInHistory(id, newPassword)) {
            throw new InvalidPasswordException("Cannot reuse last 5 passwords");
        }
        
        String encoded = passwordEncoder.encode(newPassword);
        user.setPassword(encoded);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        
        addToPasswordHistory(id, encoded);
    }

    public String generateSecurePassword(int length) {
        if (length < MIN_PASSWORD_LENGTH) {
            length = MIN_PASSWORD_LENGTH;
        }
        
        StringBuilder password = new StringBuilder(length);
        String allChars = CHAR_UPPER + CHAR_LOWER + CHAR_DIGIT + CHAR_SPECIAL;
        
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_DIGIT.charAt(random.nextInt(CHAR_DIGIT.length())));
        password.append(CHAR_SPECIAL.charAt(random.nextInt(CHAR_SPECIAL.length())));
        
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        return shuffleString(password.toString());
    }

    public boolean isPasswordExpired(String userId) {
        UUID id = UUID.fromString(userId);
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return true;
        }
        
        User user = userOpt.get();
        if (user.getPasswordChangedAt() == null) {
            return true;
        }
        
        LocalDateTime expiryDate = user.getPasswordChangedAt().plusDays(PASSWORD_EXPIRY_DAYS);
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean mustChangePasswordOnLogin(String userId) {
        UUID id = UUID.fromString(userId);
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(User::getMustChangePassword).orElse(false);
    }

    public boolean isAccountLocked(String userId) {
        UUID id = UUID.fromString(userId);
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        if (user.getLockedUntil() == null) {
            return false;
        }
        
        return LocalDateTime.now().isBefore(user.getLockedUntil());
    }

    @Transactional
    public void resetFailedLoginAttempts(String userId) {
        UUID id = UUID.fromString(userId);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    @Transactional
    private void incrementFailedLoginAttempts(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(ACCOUNT_LOCK_DURATION_MINUTES));
        }
        
        userRepository.save(user);
    }

    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}
EOF

echo "✅ Fixed PasswordPolicyService.java"

# Fix 2: UserService - return UserListResponse instead of Page
cat > src/main/java/com/geriatriccare/service/UserService.java << 'EOF'
package com.geriatriccare.service;

import com.geriatriccare.dto.user.*;
import com.geriatriccare.entity.User;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.enums.UserStatus;
import com.geriatriccare.exception.InvalidPasswordException;
import com.geriatriccare.exception.ResourceNotFoundException;
import com.geriatriccare.repository.UserRepository;
import com.geriatriccare.service.security.PasswordPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PasswordPolicyService passwordPolicyService;

    @Transactional
    public UserResponse createUser(UserRequest request, String createdBy) {
        log.info("Creating user: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        passwordPolicyService.validatePassword(request.getPassword(), request.getUsername());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE);
        user.setMfaEnabled(request.getMfaEnabled() != null ? request.getMfaEnabled() : false);
        user.setMustChangePassword(request.getMustChangePassword() != null ? request.getMustChangePassword() : false);
        user.setEmailVerified(false);
        user.setDeleted(false);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setCreatedBy(createdBy);
        
        user = userRepository.save(user);
        passwordPolicyService.addToPasswordHistory(user.getId(), user.getPassword());
        
        log.info("User created: {}", user.getId());
        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserListResponse getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> userPage = userRepository.findAll(pageable).map(this::convertToResponse);
        return UserListResponse.from(userPage);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserListResponse getUsersByRole(UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> userPage = userRepository.findByRole(role, pageable).map(this::convertToResponse);
        return UserListResponse.from(userPage);
    }

    @Transactional(readOnly = true)
    public UserListResponse getUsersByStatus(UserStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> userPage = userRepository.findByStatus(status, pageable).map(this::convertToResponse);
        return UserListResponse.from(userPage);
    }

    @Transactional(readOnly = true)
    public UserListResponse searchUsers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> userPage = userRepository.searchUsers(query, pageable).map(this::convertToResponse);
        return UserListResponse.from(userPage);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserRequest request, String updatedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        user.setUpdatedBy(updatedBy);
        
        user = userRepository.save(user);
        return convertToResponse(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password incorrect");
        }

        passwordPolicyService.validatePassword(request.getNewPassword(), user.getUsername());

        if (passwordPolicyService.isPasswordInHistory(userId, request.getNewPassword())) {
            throw new InvalidPasswordException("Cannot reuse last 5 passwords");
        }

        String encoded = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encoded);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userRepository.save(user);
        passwordPolicyService.addToPasswordHistory(userId, encoded);
    }

    @Transactional
    public void deactivateUser(UUID userId, String deactivatedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedBy(deactivatedBy);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(UUID userId, String activatedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedBy(activatedBy);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setDeleted(true);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void assignRole(UUID userId, UserRole role, String assignedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        user.setUpdatedBy(assignedBy);
        userRepository.save(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));
        
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Verification token expired");
        }
        
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerificationEmail(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        
        log.info("Verification email sent to: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        UUID userId = getCurrentUserId();
        return getUserById(userId);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(UUID userId, UserProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        
        user = userRepository.save(user);
        return convertToProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics() {
        UserStatisticsResponse stats = new UserStatisticsResponse();
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countActiveUsers());
        stats.setAdminCount(userRepository.countByRole(UserRole.ADMIN));
        stats.setPhysicianCount(userRepository.countByRole(UserRole.PHYSICIAN));
        stats.setCaregiverCount(userRepository.countByRole(UserRole.CAREGIVER));
        stats.setFamilyCount(userRepository.countByRole(UserRole.FAMILY));
        stats.setPatientCount(userRepository.countByRole(UserRole.PATIENT));
        stats.setPendingVerification(userRepository.countByStatus(UserStatus.PENDING_VERIFICATION));
        return stats;
    }

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }

    private UserResponse convertToResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setEmail(user.getEmail());
        r.setFirstName(user.getFirstName());
        r.setLastName(user.getLastName());
        r.setPhoneNumber(user.getPhoneNumber());
        r.setRole(user.getRole());
        r.setStatus(user.getStatus());
        r.setEmailVerified(user.getEmailVerified());
        r.setMfaEnabled(user.getMfaEnabled());
        r.setCreatedAt(user.getCreatedAt());
        r.setUpdatedAt(user.getUpdatedAt());
        return r;
    }

    private UserProfileResponse convertToProfileResponse(User user) {
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setPhoneNumber(user.getPhoneNumber());
        profile.setRole(user.getRole());
        profile.setProfilePictureUrl(user.getProfilePictureUrl());
        profile.setEmailVerified(user.getEmailVerified());
        profile.setMfaEnabled(user.getMfaEnabled());
        profile.setCreatedAt(user.getCreatedAt());
        return profile;
    }
}
EOF

echo "✅ Fixed UserService.java"

echo ""
echo "🔨 Compiling..."
mvn clean compile -DskipTests

echo ""
echo "✅ All fixes applied!"