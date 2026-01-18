package com.geriatriccare.dto.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ComplianceStatus {
    
    private Boolean hipaaCompliant;
    private Integer auditLogRetentionDays;
    private Integer requiredRetentionDays;
    private Boolean tamperProofingEnabled;
    private Boolean phiAccessLogging;
    private Boolean securityEventMonitoring;
    private List<String> complianceIssues;
    private List<String> recommendations;
    private LocalDateTime lastAuditDate;
    private LocalDateTime nextAuditDate;
    
    public ComplianceStatus() {
        this.complianceIssues = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.requiredRetentionDays = 2555; // 7 years HIPAA requirement
        this.hipaaCompliant = false;
    }
    
    // Getters and Setters
    public Boolean getHipaaCompliant() { return hipaaCompliant; }
    public void setHipaaCompliant(Boolean hipaaCompliant) { this.hipaaCompliant = hipaaCompliant; }
    
    public Integer getAuditLogRetentionDays() { return auditLogRetentionDays; }
    public void setAuditLogRetentionDays(Integer auditLogRetentionDays) { this.auditLogRetentionDays = auditLogRetentionDays; }
    
    public Integer getRequiredRetentionDays() { return requiredRetentionDays; }
    public void setRequiredRetentionDays(Integer requiredRetentionDays) { this.requiredRetentionDays = requiredRetentionDays; }
    
    public Boolean getTamperProofingEnabled() { return tamperProofingEnabled; }
    public void setTamperProofingEnabled(Boolean tamperProofingEnabled) { this.tamperProofingEnabled = tamperProofingEnabled; }
    
    public Boolean getPhiAccessLogging() { return phiAccessLogging; }
    public void setPhiAccessLogging(Boolean phiAccessLogging) { this.phiAccessLogging = phiAccessLogging; }
    
    public Boolean getSecurityEventMonitoring() { return securityEventMonitoring; }
    public void setSecurityEventMonitoring(Boolean securityEventMonitoring) { this.securityEventMonitoring = securityEventMonitoring; }
    
    public List<String> getComplianceIssues() { return complianceIssues; }
    public void setComplianceIssues(List<String> complianceIssues) { this.complianceIssues = complianceIssues; }
    
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    
    public LocalDateTime getLastAuditDate() { return lastAuditDate; }
    public void setLastAuditDate(LocalDateTime lastAuditDate) { this.lastAuditDate = lastAuditDate; }
    
    public LocalDateTime getNextAuditDate() { return nextAuditDate; }
    public void setNextAuditDate(LocalDateTime nextAuditDate) { this.nextAuditDate = nextAuditDate; }
}
