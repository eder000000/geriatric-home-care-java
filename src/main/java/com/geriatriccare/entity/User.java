package com.geriatriccare.entity;

import com.geriatriccare.enums.UserRole;
import com.geriatriccare.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Entity
 * Represents a system user with role-based access control
 * 
 * Supports five roles: ADMIN, PHYSICIAN, CAREGIVER, FAMILY, PATIENT
 * Integrates with Sprint 5 security features (MFA, Password Policy, Sessions)
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_role", columnList = "role"),
    @Index(name = "idx_user_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /**
     * Password hash (BCrypt from Sprint 5)
     * Never store plain-text passwords
     */
    @NotBlank(message = "Password is required")
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String lastName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(length = 20)
    private String phoneNumber;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    /**
     * Email verification flag
     */
    @Column(nullable = false)
    private Boolean emailVerified = false;

    /**
     * Email verification token
     */
    @Column(length = 100)
    private String emailVerificationToken;

    /**
     * Token expiration
     */
    private LocalDateTime emailVerificationTokenExpiry;

    /**
     * MFA enabled flag (integrates with Sprint 5 MFA)
     */
    @Column(nullable = false)
    private Boolean mfaEnabled = false;

    /**
     * MFA secret (for TOTP)
     */
    @Column(length = 100)
    private String mfaSecret;

    /**
     * Profile picture URL
     */
    @Column(length = 500)
    private String profilePictureUrl;

    /**
     * User preferences (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String preferences;

    /**
     * Last successful login timestamp
     */
    private LocalDateTime lastLoginAt;

    /**
     * Last login IP address
     */
    @Column(length = 45)
    private String lastLoginIp;

    /**
     * Failed login attempts counter (integrates with Sprint 5 Password Policy)
     */
    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;

    /**
     * Account locked until (for failed login attempts)
     */
    private LocalDateTime lockedUntil;

    /**
     * Password last changed date (integrates with Sprint 5 Password Policy)
     */
    private LocalDateTime passwordChangedAt;

    /**
     * Password must be changed on next login
     */
    @Column(nullable = false)
    private Boolean mustChangePassword = false;

    // Audit fields

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * User who created this account
     */
    @Column(length = 50)
    private String createdBy;

    /**
     * User who last updated this account
     */
    @Column(length = 50)
    private String updatedBy;

    /**
     * Soft delete flag
     */
    @Column(nullable = false)
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    private String deletedBy;

    // Computed properties

    /**
     * Get full name
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if account is active and can login
     */
    @Transient
    public boolean canLogin() {
        return status == UserStatus.ACTIVE 
            && emailVerified 
            && !deleted
            && (lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now()));
    }

    /**
     * Check if account is locked due to failed login attempts
     */
    @Transient
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Check if password has expired (90 days from Sprint 5)
     */
    @Transient
    public boolean isPasswordExpired() {
        if (passwordChangedAt == null) {
            return false;
        }
        return passwordChangedAt.plusDays(90).isBefore(LocalDateTime.now());
    }

    /**
     * Check if user has specific role
     */
    @Transient
    public boolean hasRole(UserRole checkRole) {
        return this.role == checkRole;
    }

    /**
     * Check if user has any of the specified roles
     */
    @Transient
    public boolean hasAnyRole(UserRole... roles) {
        for (UserRole r : roles) {
            if (this.role == r) {
                return true;
            }
        }
        return false;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (passwordChangedAt == null) {
            passwordChangedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
