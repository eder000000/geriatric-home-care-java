package com.geriatriccare.dto.security;

import java.time.LocalDateTime;

public class SessionStatistics {
    
    private Integer totalActiveSessions;
    private Integer totalUsers;
    private Integer expiredSessionsToday;
    private Integer revokedSessionsToday;
    private Double averageSessionDurationMinutes;
    private Integer peakConcurrentSessions;
    private LocalDateTime generatedAt;
    
    public SessionStatistics() {
        this.generatedAt = LocalDateTime.now();
        this.totalActiveSessions = 0;
        this.totalUsers = 0;
        this.expiredSessionsToday = 0;
        this.revokedSessionsToday = 0;
    }
    
    // Getters and Setters
    public Integer getTotalActiveSessions() { return totalActiveSessions; }
    public void setTotalActiveSessions(Integer totalActiveSessions) { this.totalActiveSessions = totalActiveSessions; }
    
    public Integer getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
    
    public Integer getExpiredSessionsToday() { return expiredSessionsToday; }
    public void setExpiredSessionsToday(Integer expiredSessionsToday) { this.expiredSessionsToday = expiredSessionsToday; }
    
    public Integer getRevokedSessionsToday() { return revokedSessionsToday; }
    public void setRevokedSessionsToday(Integer revokedSessionsToday) { this.revokedSessionsToday = revokedSessionsToday; }
    
    public Double getAverageSessionDurationMinutes() { return averageSessionDurationMinutes; }
    public void setAverageSessionDurationMinutes(Double averageSessionDurationMinutes) { this.averageSessionDurationMinutes = averageSessionDurationMinutes; }
    
    public Integer getPeakConcurrentSessions() { return peakConcurrentSessions; }
    public void setPeakConcurrentSessions(Integer peakConcurrentSessions) { this.peakConcurrentSessions = peakConcurrentSessions; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
