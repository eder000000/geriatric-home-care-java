package com.geriatriccare.dto;

import com.geriatriccare.entity.CarePlanPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;

import java.time.LocalDate;
import java.util.UUID;

public class CarePlanRequest {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Priority is required")
    private CarePlanPriority priority;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    private UUID assignedCaregiverId;
    
    // Constructors
    public CarePlanRequest() {}
    
    // Getters and Setters
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public CarePlanPriority getPriority() { return priority; }
    public void setPriority(CarePlanPriority priority) { this.priority = priority; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public UUID getAssignedCaregiverId() { return assignedCaregiverId; }
    public void setAssignedCaregiverId(UUID assignedCaregiverId) { this.assignedCaregiverId = assignedCaregiverId; }
}