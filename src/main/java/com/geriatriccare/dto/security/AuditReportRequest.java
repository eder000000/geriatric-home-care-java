package com.geriatriccare.dto.security;

import java.time.LocalDateTime;

public class AuditReportRequest {
    
    private String reportType; // COMPLIANCE, SECURITY, USER_ACTIVITY, PHI_ACCESS
    private String format; // PDF, CSV, JSON
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String userId;
    private Boolean includeDetails;
    
    public AuditReportRequest() {
        this.reportType = "COMPLIANCE";
        this.format = "PDF";
        this.includeDetails = true;
    }
    
    // Getters and Setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Boolean getIncludeDetails() { return includeDetails; }
    public void setIncludeDetails(Boolean includeDetails) { this.includeDetails = includeDetails; }
}
