package com.geriatriccare.dto.security;

import java.util.ArrayList;
import java.util.List;

public class SessionListResponse {
    
    private List<UserSession> sessions;
    private Integer totalSessions;
    private Integer activeSessions;
    private SessionConfiguration configuration;
    
    public SessionListResponse() {
        this.sessions = new ArrayList<>();
        this.totalSessions = 0;
        this.activeSessions = 0;
    }
    
    // Getters and Setters
    public List<UserSession> getSessions() { return sessions; }
    public void setSessions(List<UserSession> sessions) { this.sessions = sessions; }
    
    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
    
    public Integer getActiveSessions() { return activeSessions; }
    public void setActiveSessions(Integer activeSessions) { this.activeSessions = activeSessions; }
    
    public SessionConfiguration getConfiguration() { return configuration; }
    public void setConfiguration(SessionConfiguration configuration) { this.configuration = configuration; }
}
