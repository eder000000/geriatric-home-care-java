package com.geriatriccare.dto.security;

import com.geriatriccare.enums.AuditEventType;
import com.geriatriccare.enums.AuditSeverity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;



public class AuditFilterRequest {
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> userIds;
    private List<UUID> patientIds;
    private List<com.geriatriccare.enums.AuditEventType> eventTypes;
    private List<AuditSeverity> severities;
    private List<DataSensitivity> dataSensitivities;
    private String ipAddress;
    private String searchText;
    private Integer page;
    private Integer pageSize;
    private String sortBy;
    private String sortDirection;
    
    public AuditFilterRequest() {
        this.page = 0;
        this.pageSize = 50;
        this.sortBy = "timestamp";
        this.sortDirection = "DESC";
    }
    
    // Getters and Setters
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public List<String> getUserIds() { return userIds; }
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }
    
    public List<UUID> getPatientIds() { return patientIds; }
    public void setPatientIds(List<UUID> patientIds) { this.patientIds = patientIds; }
    
    public List<com.geriatriccare.enums.AuditEventType> getEventTypes() { return eventTypes; }
    public void setEventTypes(List<com.geriatriccare.enums.AuditEventType> eventTypes) { this.eventTypes = eventTypes; }
    
    public List<AuditSeverity> getSeverities() { return severities; }
    public void setSeverities(List<AuditSeverity> severities) { this.severities = severities; }
    
    public List<DataSensitivity> getDataSensitivities() { return dataSensitivities; }
    public void setDataSensitivities(List<DataSensitivity> dataSensitivities) { this.dataSensitivities = dataSensitivities; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getSearchText() { return searchText; }
    public void setSearchText(String searchText) { this.searchText = searchText; }
    
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    
    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}
