package com.geriatriccare.dto.security;

import com.geriatriccare.enums.AuditSeverity;
import java.time.LocalDateTime;
import java.util.UUID;

public class ComplianceViolation {
    
    private UUID violationId;
    private String violationType;
    private AuditSeverity severity;
    private String description;
    private String userId;
    private UUID patientId;
    private String details;
    private LocalDateTime detectedAt;
    private Boolean resolved;
    private String resolution;
    
    public ComplianceViolation() {
        this.violationId = UUID.randomUUID();
        this.detectedAt = LocalDateTime.now();
        this.resolved = false;
    }
    
    // Getters and Setters
    public UUID getViolationId() { return violationId; }
    public void setViolationId(UUID violationId) { this.violationId = violationId; }
    
    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    
    public AuditSeverity getSeverity() { return severity; }
    public void setSeverity(AuditSeverity severity) { this.severity = severity; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    
    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }
    
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}
