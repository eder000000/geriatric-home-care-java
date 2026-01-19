package com.geriatriccare.dto.security;

import java.time.LocalDateTime;

public class RateLimitInfo {
    
    private String identifier;
    private Integer limit;
    private Integer remaining;
    private LocalDateTime resetTime;
    private Boolean isBlocked;
    
    public RateLimitInfo() {
        this.isBlocked = false;
    }
    
    // Getters and Setters
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    
    public Integer getRemaining() { return remaining; }
    public void setRemaining(Integer remaining) { this.remaining = remaining; }
    
    public LocalDateTime getResetTime() { return resetTime; }
    public void setResetTime(LocalDateTime resetTime) { this.resetTime = resetTime; }
    
    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }
}
