package com.geriatriccare.dto.user;

import com.geriatriccare.entity.User;
import com.geriatriccare.enums.UserRole;
import com.geriatriccare.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Profile Response DTO
 * Detailed user profile with preferences
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private String roleDisplayName;
    private String roleDescription;
    private UserStatus status;
    private Boolean emailVerified;
    private Boolean mfaEnabled;
    private String profilePictureUrl;
    private String preferences;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Integer failedLoginAttempts;
    private Boolean isLocked;
    private Boolean isPasswordExpired;
    private Boolean mustChangePassword;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert User entity to UserProfileResponse DTO
     */
    public static UserProfileResponse from(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .roleDisplayName(user.getRole().getDisplayName())
                .roleDescription(user.getRole().getDescription())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .mfaEnabled(user.getMfaEnabled())
                .profilePictureUrl(user.getProfilePictureUrl())
                .preferences(user.getPreferences())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .isLocked(user.isLocked())
                .isPasswordExpired(user.isPasswordExpired())
                .mustChangePassword(user.getMustChangePassword())
                .passwordChangedAt(user.getPasswordChangedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
