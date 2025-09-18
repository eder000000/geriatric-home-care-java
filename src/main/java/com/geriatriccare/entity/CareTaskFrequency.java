package com.geriatriccare.entity;

public enum CareTaskFrequency {
    ONCE("One time only"),
    DAILY("Daily"),
    TWICE_DAILY("Twice daily"),
    THREE_TIMES_DAILY("Three times daily"),
    WEEKLY("Weekly"),
    TWICE_WEEKLY("Twice weekly"),
    MONTHLY("Monthly"),
    AS_NEEDED("As needed"),
    CUSTOM("Custom schedule");

    private final String displayName;

    CareTaskFrequency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRegularSchedule() {
        return this != AS_NEEDED && this != CUSTOM && this != ONCE;
    }

    public int getDailyOccurrences() {
        return switch (this) {
            case DAILY -> 1;
            case TWICE_DAILY -> 2;
            case THREE_TIMES_DAILY -> 3;
            default -> 0;
        };
    }
}