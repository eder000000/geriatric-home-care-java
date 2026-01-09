package com.geriatriccare.dto.ai;

public enum CarePlanFocusArea {
    MEDICAL_MANAGEMENT("Medical Management", 
            "Medication management, chronic disease management, symptom control"),
    
    DAILY_ACTIVITIES("Activities of Daily Living", 
            "Personal care, mobility, bathing, dressing, grooming"),
    
    NUTRITION("Nutrition and Hydration", 
            "Diet planning, meal preparation, hydration monitoring, nutritional supplements"),
    
    EXERCISE("Physical Activity and Exercise", 
            "Exercise routines, physical therapy, mobility exercises, fall prevention"),
    
    SAFETY("Safety and Fall Prevention", 
            "Home safety modifications, fall risk assessment, emergency preparedness"),
    
    COGNITIVE("Cognitive and Mental Health", 
            "Memory support, cognitive stimulation, mental health monitoring, behavioral management"),
    
    SOCIAL("Social Engagement", 
            "Social activities, community involvement, family interaction, isolation prevention"),
    
    PAIN_MANAGEMENT("Pain Management", 
            "Pain assessment, pain control strategies, non-pharmacological interventions"),
    
    WOUND_CARE("Wound Care", 
            "Wound assessment, dressing changes, pressure ulcer prevention"),
    
    MEDICATION_SAFETY("Medication Safety", 
            "Drug interactions, adverse effects monitoring, medication adherence"),
    
    COMMUNICATION("Communication Support", 
            "Communication strategies, assistive devices, healthcare coordination"),
    
    END_OF_LIFE("End of Life Care", 
            "Advance directives, palliative care, hospice planning, comfort measures");
    
    private final String displayName;
    private final String description;
    
    CarePlanFocusArea(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
