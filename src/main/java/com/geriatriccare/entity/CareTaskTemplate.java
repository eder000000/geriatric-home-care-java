package com.geriatriccare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "care_task_templates")
public class CareTaskTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String taskName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareTaskCategory category;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareTaskPriority priority;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareTaskFrequency frequency;
    
    private LocalTime scheduledTime;
    
    @Positive
    private Integer estimatedDurationMinutes;
    
    @Column(nullable = false)
    private Boolean requiresCaregiverPresence = false;
    
    @Column(columnDefinition = "TEXT")
    private String instructions;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private CarePlanTemplate template;
    
    // Constructors
    public CareTaskTemplate() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public CarePlanTemplate getTemplate() { return template; }
    public void setTemplate(CarePlanTemplate template) { this.template = template; }
}