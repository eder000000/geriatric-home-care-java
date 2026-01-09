package com.geriatriccare.dto.ai;

public enum InteractionSeverity {
    CONTRAINDICATED("Contraindicated", 
            "Combination is contraindicated - DO NOT USE TOGETHER",
            "Immediate action required - stop one or both medications",
            4),
    
    MAJOR("Major", 
            "Major interaction - serious clinical consequences possible",
            "Consider alternative therapy or close monitoring with dose adjustment",
            3),
    
    MODERATE("Moderate", 
            "Moderate interaction - monitor closely",
            "Monitor patient carefully, may need dose adjustment or timing changes",
            2),
    
    MINOR("Minor", 
            "Minor interaction - minimal clinical significance",
            "Awareness needed, usually no intervention required",
            1);
    
    private final String displayName;
    private final String description;
    private final String recommendation;
    private final int riskLevel;
    
    InteractionSeverity(String displayName, String description, String recommendation, int riskLevel) {
        this.displayName = displayName;
        this.description = description;
        this.recommendation = recommendation;
        this.riskLevel = riskLevel;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public int getRiskLevel() {
        return riskLevel;
    }
}
