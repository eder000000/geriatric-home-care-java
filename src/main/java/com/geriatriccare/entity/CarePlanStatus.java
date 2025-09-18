package com.geriatriccare.entity;

public enum CarePlanStatus {
    DRAFT("Draft"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    SUSPENDED("Suspended"),
    CANCELLED("Cancelled");

    private final String displayName;

    CarePlanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isExecutable() {
        return this == ACTIVE;
    }

    public boolean isEditable() {
        return this == DRAFT || this == ACTIVE;
    }
}