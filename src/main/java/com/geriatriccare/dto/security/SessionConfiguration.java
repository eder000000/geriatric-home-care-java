package com.geriatriccare.dto.security;

public class SessionConfiguration {
    
    private Integer timeoutMinutes;
    private Integer rememberMeDays;
    private Integer maxConcurrentSessions;
    private Boolean autoCleanup;
    private Integer cleanupIntervalMinutes;
    
    public SessionConfiguration() {
        this.timeoutMinutes = 30;
        this.rememberMeDays = 30;
        this.maxConcurrentSessions = 3;
        this.autoCleanup = true;
        this.cleanupIntervalMinutes = 15;
    }
    
    // Getters and Setters
    public Integer getTimeoutMinutes() { return timeoutMinutes; }
    public void setTimeoutMinutes(Integer timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
    
    public Integer getRememberMeDays() { return rememberMeDays; }
    public void setRememberMeDays(Integer rememberMeDays) { this.rememberMeDays = rememberMeDays; }
    
    public Integer getMaxConcurrentSessions() { return maxConcurrentSessions; }
    public void setMaxConcurrentSessions(Integer maxConcurrentSessions) { this.maxConcurrentSessions = maxConcurrentSessions; }
    
    public Boolean getAutoCleanup() { return autoCleanup; }
    public void setAutoCleanup(Boolean autoCleanup) { this.autoCleanup = autoCleanup; }
    
    public Integer getCleanupIntervalMinutes() { return cleanupIntervalMinutes; }
    public void setCleanupIntervalMinutes(Integer cleanupIntervalMinutes) { this.cleanupIntervalMinutes = cleanupIntervalMinutes; }
}
