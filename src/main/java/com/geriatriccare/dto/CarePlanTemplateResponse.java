package com.geriatriccare.dto;

import com.geriatriccare.dto.summary.CaregiverSummary;
import com.geriatriccare.entity.CarePlanPriority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CarePlanTemplateResponse {
    
    private UUID id;
    private String name;
    private String description;
    private CarePlanPriority defaultPriority;
    private String category;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CaregiverSummary createdBy;
    private List<CareTaskTemplateSummary> taskTemplates = new ArrayList<>();
    private int taskCount;
    
    // Constructors
    public CarePlanTemplateResponse() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public CarePlanPriority getDefaultPriority() { return defaultPriority; }
    public void setDefaultPriority(CarePlanPriority defaultPriority) { 
        this.defaultPriority = defaultPriority; 
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public CaregiverSummary getCreatedBy() { return createdBy; }
    public void setCreatedBy(CaregiverSummary createdBy) { this.createdBy = createdBy; }
    
    public List<CareTaskTemplateSummary> getTaskTemplates() { return taskTemplates; }
    public void setTaskTemplates(List<CareTaskTemplateSummary> taskTemplates) { 
        this.taskTemplates = taskTemplates; 
    }
    
    public int getTaskCount() { return taskCount; }
    public void setTaskCount(int taskCount) { this.taskCount = taskCount; }
    
    // Nested class for task template summaries
    public static class CareTaskTemplateSummary {
        private UUID id;
        private String taskName;
        private String category;
        private String priority;
        private String frequency;
        private java.time.LocalTime scheduledTime;
        
        public CareTaskTemplateSummary() {}
        
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public java.time.LocalTime getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(java.time.LocalTime scheduledTime) { 
            this.scheduledTime = scheduledTime; 
        }
    }
}
