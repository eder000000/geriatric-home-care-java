package com.geriatriccare.entity;

/**
 * Frequency options for care tasks
 */
public enum CareTaskFrequency {
    ONCE("Once"),
    DAILY("Daily"),
    TWICE_DAILY("Twice Daily"),
    THREE_TIMES_DAILY("Three Times Daily"),
    FOUR_TIMES_DAILY("Four Times Daily"),
    WEEKLY("Weekly"),
    BIWEEKLY("Biweekly"),
    MONTHLY("Monthly"),
    AS_NEEDED("As Needed");

    private final String displayName;

    CareTaskFrequency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}