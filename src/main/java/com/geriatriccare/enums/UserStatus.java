package com.geriatriccare.enums;

/**
 * User Status Enumeration
 * Tracks the lifecycle status of user accounts
 */
public enum UserStatus {
    ACTIVE("Active", "User account is active and can access the system"),
    INACTIVE("Inactive", "User account is inactive (temporarily disabled)"),
    SUSPENDED("Suspended", "User account is suspended (administrative action)"),
    PENDING_VERIFICATION("Pending Verification", "User account awaiting email verification");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if user can login with this status
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * Check if status requires action
     */
    public boolean requiresAction() {
        return this == PENDING_VERIFICATION;
    }
}
