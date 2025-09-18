package com.geriatriccare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "care_tasks")
@EntityListeners(AuditingEntityListener.class)
public class CareTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_plan_id", nullable = false)
    @NotNull(message = "Care plan is required")
    private CarePlan carePlan;

    @Column(nullable = false)
    @NotBlank(message = "Task name is required")
    @Size(max = 200, message = "Task name cannot exceed 200 characters")
    private String taskName;

    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "Task description cannot exceed 1000 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareTaskCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareTaskFrequency frequency;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareTaskPriority priority = CareTaskPriority.MEDIUM;

    @Column(name = "requires_caregiver_presence")
    private Boolean requiresCaregiverPresence = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CareTask() {}

    public CareTask(CarePlan carePlan, String taskName, CareTaskCategory category, CareTaskFrequency frequency) {
        this.carePlan = carePlan;
        this.taskName = taskName;
        this.category = category;
        this.frequency = frequency;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CarePlan getCarePlan() { return carePlan; }
    public void setCarePlan(CarePlan carePlan) { this.carePlan = carePlan; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CareTaskCategory getCategory() { return category; }
    public void setCategory(CareTaskCategory category) { this.category = category; }

    public CareTaskFrequency getFrequency() { return frequency; }
    public void setFrequency(CareTaskFrequency frequency) { this.frequency = frequency; }

    public LocalTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public CareTaskPriority getPriority() { return priority; }
    public void setPriority(CareTaskPriority priority) { this.priority = priority; }

    public Boolean getRequiresCaregiverPresence() { return requiresCaregiverPresence; }
    public void setRequiresCaregiverPresence(Boolean requiresCaregiverPresence) { this.requiresCaregiverPresence = requiresCaregiverPresence; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business methods
    public boolean isScheduledTask() {
        return scheduledTime != null;
    }

    public boolean isHighPriority() {
        return priority == CareTaskPriority.HIGH || priority == CareTaskPriority.URGENT;
    }

    public String getDisplayName() {
        return taskName + " (" + category.getDisplayName() + ")";
    }
}