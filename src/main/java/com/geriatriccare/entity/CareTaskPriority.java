package com.geriatriccare.entity;

public enum CareTaskPriority {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4);

    private final String displayName;
    private final int level;

    CareTaskPriority(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherPriorityThan(CareTaskPriority other) {
        return this.level > other.level;
    }

    public boolean requiresImmediateAttention() {
        return this == HIGH || this == URGENT;
    }
}