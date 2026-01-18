package com.geriatriccare.dto.security;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AuditReportResponse {
    
    private String reportId;
    private String reportType;
    private String format;
    private LocalDateTime generatedAt;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Map<String, Object> summary;
    private Map<String, Integer> eventCounts;
    private Integer totalEvents;
    private Integer criticalEvents;
    private Integer securityIncidents;
    private String downloadUrl;
    
    public AuditReportResponse() {
        this.summary = new HashMap<>();
        this.eventCounts = new HashMap<>();
        this.generatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
    
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
    
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }
    
    public Map<String, Integer> getEventCounts() { return eventCounts; }
    public void setEventCounts(Map<String, Integer> eventCounts) { this.eventCounts = eventCounts; }
    
    public Integer getTotalEvents() { return totalEvents; }
    public void setTotalEvents(Integer totalEvents) { this.totalEvents = totalEvents; }
    
    public Integer getCriticalEvents() { return criticalEvents; }
    public void setCriticalEvents(Integer criticalEvents) { this.criticalEvents = criticalEvents; }
    
    public Integer getSecurityIncidents() { return securityIncidents; }
    public void setSecurityIncidents(Integer securityIncidents) { this.securityIncidents = securityIncidents; }
    
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
