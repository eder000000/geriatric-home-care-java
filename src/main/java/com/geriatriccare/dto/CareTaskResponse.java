package com.geriatriccare.dto;

import com.geriatriccare.entity.CareTaskCategory;
import com.geriatriccare.entity.CareTaskFrequency;
import com.geriatriccare.entity.CareTaskPriority;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class CareTaskResponse {
    
    private UUID id;
    private UUID carePlanId;
    private String carePlanTitle;
    private String taskName;
    private String description;
    private CareTaskCategory category;
    private CareTaskPriority priority;
    private CareTaskFrequency frequency;
    private LocalTime scheduledTime;
    private Integer estimatedDurationMinutes;
    private Boolean requiresCaregiverPresence;
    private String instructions;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Completion tracking
    private Boolean isCompleted;
    private LocalDateTime lastCompletedAt;
    private LocalDateTime nextDueDate;
    private Boolean isOverdue;
    
    // Constructors
    public CareTaskResponse() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getCarePlanId() { return carePlanId; }
    public void setCarePlanId(UUID carePlanId) { this.carePlanId = carePlanId; }
    
    public String getCarePlanTitle() { return carePlanTitle; }
    public void setCarePlanTitle(String carePlanTitle) { this.carePlanTitle = carePlanTitle; }
    
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
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    
    public LocalDateTime getLastCompletedAt() { return lastCompletedAt; }
    public void setLastCompletedAt(LocalDateTime lastCompletedAt) { 
        this.lastCompletedAt = lastCompletedAt; 
    }
    
    public LocalDateTime getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDateTime nextDueDate) { this.nextDueDate = nextDueDate; }
    
    public Boolean getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }
}