package com.geriatriccare.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public class TemplateInstantiationRequest {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private UUID assignedCaregiverId;
    
    // Constructors
    public TemplateInstantiationRequest() {}
    
    // Getters and Setters
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public UUID getAssignedCaregiverId() { return assignedCaregiverId; }
    public void setAssignedCaregiverId(UUID assignedCaregiverId) { 
        this.assignedCaregiverId = assignedCaregiverId; 
    }
}