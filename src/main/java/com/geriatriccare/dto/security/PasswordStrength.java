package com.geriatriccare.dto.security;

public enum PasswordStrength {
    WEAK("Weak", "Does not meet minimum requirements", 1),
    FAIR("Fair", "Meets basic requirements but could be stronger", 2),
    GOOD("Good", "Meets all requirements", 3),
    STRONG("Strong", "Exceeds requirements with additional complexity", 4),
    VERY_STRONG("Very Strong", "Maximum security password", 5);
    
    private final String displayName;
    private final String description;
    private final int score;
    
    PasswordStrength(String displayName, String description, int score) {
        this.displayName = displayName;
        this.description = description;
        this.score = score;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getScore() { return score; }
}
