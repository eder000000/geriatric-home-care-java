package com.geriatriccare.dto.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuditReport {
    
    private String reportId;
    private String reportType;
    private LocalDateTime generatedAt;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Integer totalEvents;
    private Integer phiAccessCount;
    private Integer securityEventsCount;
    private Integer failedLoginsCount;
    private Integer unauthorizedAccessCount;
    private List<AuditEvent> events;
    private Map<String, Integer> eventTypeBreakdown;
    private Map<String, Integer> userActivityBreakdown;
    private Map<String, Integer> severityBreakdown;
    private List<String> complianceViolations;
    private String summary;
    
    public AuditReport() {
        this.reportId = java.util.UUID.randomUUID().toString();
        this.generatedAt = LocalDateTime.now();
        this.events = new ArrayList<>();
        this.eventTypeBreakdown = new HashMap<>();
        this.userActivityBreakdown = new HashMap<>();
        this.severityBreakdown = new HashMap<>();
        this.complianceViolations = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
    
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
    
    public Integer getTotalEvents() { return totalEvents; }
    public void setTotalEvents(Integer totalEvents) { this.totalEvents = totalEvents; }
    
    public Integer getPhiAccessCount() { return phiAccessCount; }
    public void setPhiAccessCount(Integer phiAccessCount) { this.phiAccessCount = phiAccessCount; }
    
    public Integer getSecurityEventsCount() { return securityEventsCount; }
    public void setSecurityEventsCount(Integer securityEventsCount) { this.securityEventsCount = securityEventsCount; }
    
    public Integer getFailedLoginsCount() { return failedLoginsCount; }
    public void setFailedLoginsCount(Integer failedLoginsCount) { this.failedLoginsCount = failedLoginsCount; }
    
    public Integer getUnauthorizedAccessCount() { return unauthorizedAccessCount; }
    public void setUnauthorizedAccessCount(Integer unauthorizedAccessCount) { this.unauthorizedAccessCount = unauthorizedAccessCount; }
    
    public List<AuditEvent> getEvents() { return events; }
    public void setEvents(List<AuditEvent> events) { this.events = events; }
    
    public Map<String, Integer> getEventTypeBreakdown() { return eventTypeBreakdown; }
    public void setEventTypeBreakdown(Map<String, Integer> eventTypeBreakdown) { this.eventTypeBreakdown = eventTypeBreakdown; }
    
    public Map<String, Integer> getUserActivityBreakdown() { return userActivityBreakdown; }
    public void setUserActivityBreakdown(Map<String, Integer> userActivityBreakdown) { this.userActivityBreakdown = userActivityBreakdown; }
    
    public Map<String, Integer> getSeverityBreakdown() { return severityBreakdown; }
    public void setSeverityBreakdown(Map<String, Integer> severityBreakdown) { this.severityBreakdown = severityBreakdown; }
    
    public List<String> getComplianceViolations() { return complianceViolations; }
    public void setComplianceViolations(List<String> complianceViolations) { this.complianceViolations = complianceViolations; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
