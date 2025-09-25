package com.geriatriccare.dto;

import com.geriatriccare.entity.CareTaskCategory;
import com.geriatriccare.entity.CareTaskFrequency;
import com.geriatriccare.entity.CareTaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.UUID;

public class CareTaskRequest {
    
    @NotNull(message = "Care plan ID is required")
    private UUID carePlanId;
    
    @NotBlank(message = "Task name is required")
    @Size(max = 255, message = "Task name must not exceed 255 characters")
    private String taskName;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Category is required")
    private CareTaskCategory category;
    
    @NotNull(message = "Priority is required")
    private CareTaskPriority priority;
    
    @NotNull(message = "Frequency is required")
    private CareTaskFrequency frequency;
    
    private LocalTime scheduledTime;
    
    @Positive(message = "Duration must be positive")
    private Integer estimatedDurationMinutes;
    
    private Boolean requiresCaregiverPresence;
    
    @Size(max = 2000, message = "Instructions must not exceed 2000 characters")
    private String instructions;
    
    // Constructors
    public CareTaskRequest() {}
    
    // Getters and Setters
    public UUID getCarePlanId() { return carePlanId; }
    public void setCarePlanId(UUID carePlanId) { this.carePlanId = carePlanId; }
    
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public CareTaskCategory getCategory() { return category; }
    public void setCategory(CareTaskCategory category) { this.category = category; }
    
    public CareTaskPriority getPriority() { return priority; }
    public void setPriority(CareTaskPriority priority) { this.priority = priority; }
    
    public CareTaskFrequency getFrequency() { return frequency; }
    public void setFrequency(CareTaskFrequency frequency) { this.frequency = frequency; }
    
    public LocalTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { 
        this.estimatedDurationMinutes = estimatedDurationMinutes; 
    }
    
    public Boolean getRequiresCaregiverPresence() { return requiresCaregiverPresence; }
    public void setRequiresCaregiverPresence(Boolean requiresCaregiverPresence) { 
        this.requiresCaregiverPresence = requiresCaregiverPresence; 
    }
    
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}