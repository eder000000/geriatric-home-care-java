package com.geriatriccare.dto.ai;

public enum CarePlanTimeframe {
    IMMEDIATE("Immediate", "0-2 weeks", "Acute needs and urgent interventions"),
    SHORT_TERM("Short-term", "2-12 weeks", "Initial stabilization and goal setting"),
    INTERMEDIATE("Intermediate", "3-6 months", "Ongoing management and progress evaluation"),
    LONG_TERM("Long-term", "6-12 months", "Sustained care and maintenance"),
    EXTENDED("Extended", "1+ years", "Chronic condition management and quality of life");
    
    private final String displayName;
    private final String duration;
    private final String description;
    
    CarePlanTimeframe(String displayName, String duration, String description) {
        this.displayName = displayName;
        this.duration = duration;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public String getDescription() {
        return description;
    }
}
