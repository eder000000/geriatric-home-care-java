package com.geriatriccare.dto.security;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidationResponse {
    
    private Boolean isValid;
    private PasswordStrength strength;
    private Integer score;
    private List<String> violations;
    private List<String> suggestions;
    private String message;
    
    public PasswordValidationResponse() {
        this.violations = new ArrayList<>();
        this.suggestions = new ArrayList<>();
        this.isValid = false;
    }
    
    public Boolean getIsValid() { return isValid; }
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }
    
    public PasswordStrength getStrength() { return strength; }
    public void setStrength(PasswordStrength strength) { this.strength = strength; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    
    public List<String> getViolations() { return violations; }
    public void setViolations(List<String> violations) { this.violations = violations; }
    
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
