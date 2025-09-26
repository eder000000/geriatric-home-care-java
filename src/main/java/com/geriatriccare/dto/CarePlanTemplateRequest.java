package com.geriatriccare.dto;

import com.geriatriccare.entity.CarePlanPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class CarePlanTemplateRequest {
    
    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Default priority is required")
    private CarePlanPriority defaultPriority;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    private List<CareTaskTemplateRequest> taskTemplates = new ArrayList<>();
    
    // Constructors
    public CarePlanTemplateRequest() {}
    
    // Getters and Setters
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
    
    public List<CareTaskTemplateRequest> getTaskTemplates() { return taskTemplates; }
    public void setTaskTemplates(List<CareTaskTemplateRequest> taskTemplates) { 
        this.taskTemplates = taskTemplates; 
    }
    
    // Nested class for task templates
    public static class CareTaskTemplateRequest {
        @NotBlank(message = "Task name is required")
        private String taskName;
        private String description;
        @NotNull(message = "Category is required")
        private com.geriatriccare.entity.CareTaskCategory category;
        @NotNull(message = "Priority is required")
        private com.geriatriccare.entity.CareTaskPriority priority;
        @NotNull(message = "Frequency is required")
        private com.geriatriccare.entity.CareTaskFrequency frequency;
        private java.time.LocalTime scheduledTime;
        private Integer estimatedDurationMinutes;
        private Boolean requiresCaregiverPresence;
        private String instructions;
        
        // Getters and Setters
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public com.geriatriccare.entity.CareTaskCategory getCategory() { return category; }
        public void setCategory(com.geriatriccare.entity.CareTaskCategory category) { this.category = category; }
        public com.geriatriccare.entity.CareTaskPriority getPriority() { return priority; }
        public void setPriority(com.geriatriccare.entity.CareTaskPriority priority) { this.priority = priority; }
        public com.geriatriccare.entity.CareTaskFrequency getFrequency() { return frequency; }
        public void setFrequency(com.geriatriccare.entity.CareTaskFrequency frequency) { this.frequency = frequency; }
        public java.time.LocalTime getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(java.time.LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }
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
}