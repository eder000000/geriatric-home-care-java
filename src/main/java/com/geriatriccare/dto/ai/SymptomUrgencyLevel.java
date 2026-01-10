package com.geriatriccare.dto.ai;

public enum SymptomUrgencyLevel {
    EMERGENCY("Emergency - 911", 
            "Life-threatening symptoms requiring immediate emergency care",
            "Call 911 or go to emergency room immediately",
            4),
    
    URGENT("Urgent", 
            "Serious symptoms requiring prompt medical attention within hours",
            "Contact physician immediately or visit urgent care within 2-4 hours",
            3),
    
    SEMI_URGENT("Semi-Urgent", 
            "Concerning symptoms needing evaluation within 24-48 hours",
            "Schedule appointment with physician within 24-48 hours",
            2),
    
    ROUTINE("Routine", 
            "Non-urgent symptoms that can be addressed at next scheduled visit",
            "Discuss at next routine appointment or schedule within 1-2 weeks",
            1);
    
    private final String displayName;
    private final String description;
    private final String recommendation;
    private final int priorityLevel;
    
    SymptomUrgencyLevel(String displayName, String description, String recommendation, int priorityLevel) {
        this.displayName = displayName;
        this.description = description;
        this.recommendation = recommendation;
        this.priorityLevel = priorityLevel;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getRecommendation() { return recommendation; }
    public int getPriorityLevel() { return priorityLevel; }
}
