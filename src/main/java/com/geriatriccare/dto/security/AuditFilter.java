package com.geriatriccare.dto.security;

import com.geriatriccare.enums.AuditEventType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;



public class AuditFilter {
    
    private List<com.geriatriccare.enums.AuditEventType> eventTypes;
    private List<AuditEventSeverity> severities;
    private String userId;
    private UUID patientId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String ipAddress;
    private String resourceType;
    private Boolean phiEventsOnly;
    private Boolean securityEventsOnly;
    private Integer limit;
    private Integer offset;
    
    public AuditFilter() {
        this.phiEventsOnly = false;
        this.securityEventsOnly = false;
        this.limit = 100;
        this.offset = 0;
    }
    
    // Getters and Setters
    public List<com.geriatriccare.enums.AuditEventType> getEventTypes() { return eventTypes; }
    public void setEventTypes(List<com.geriatriccare.enums.AuditEventType> eventTypes) { this.eventTypes = eventTypes; }
    
    public List<AuditEventSeverity> getSeverities() { return severities; }
    public void setSeverities(List<AuditEventSeverity> severities) { this.severities = severities; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    
    public Boolean getPhiEventsOnly() { return phiEventsOnly; }
    public void setPhiEventsOnly(Boolean phiEventsOnly) { this.phiEventsOnly = phiEventsOnly; }
    
    public Boolean getSecurityEventsOnly() { return securityEventsOnly; }
    public void setSecurityEventsOnly(Boolean securityEventsOnly) { this.securityEventsOnly = securityEventsOnly; }
    
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
}
