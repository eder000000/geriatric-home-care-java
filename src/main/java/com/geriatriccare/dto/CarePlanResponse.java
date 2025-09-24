package com.geriatriccare.dto;

import com.geriatriccare.dto.summary.CaregiverSummary;
import com.geriatriccare.entity.CarePlanPriority;
import com.geriatriccare.entity.CarePlanStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CarePlanResponse {
    
    private UUID id;
    private String title;
    private String description;
    private CarePlanPriority priority;
    private CarePlanStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Patient information
    private UUID patientId;
    private String patientName;
    
    // Caregiver information
    private CaregiverSummary assignedCaregiver;
    private CaregiverSummary createdBy;
    
    // Care task summaries
    private List<CareTaskSummary> careTasks;
    private int totalTasks;
    private int completedTasks;
    private int overdueTasks;
    
    // Constructors
    public CarePlanResponse() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public CarePlanPriority getPriority() { return priority; }
    public void setPriority(CarePlanPriority priority) { this.priority = priority; }
    
    public CarePlanStatus getStatus() { return status; }
    public void setStatus(CarePlanStatus status) { this.status = status; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public CaregiverSummary getAssignedCaregiver() { return assignedCaregiver; }
    public void setAssignedCaregiver(CaregiverSummary assignedCaregiver) { this.assignedCaregiver = assignedCaregiver; }
    
    public CaregiverSummary getCreatedBy() { return createdBy; }
    public void setCreatedBy(CaregiverSummary createdBy) { this.createdBy = createdBy; }
    
    public List<CareTaskSummary> getCareTasks() { return careTasks; }
    public void setCareTasks(List<CareTaskSummary> careTasks) { this.careTasks = careTasks; }
    
    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    
    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
    
    public int getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }
    
    // Computed fields
    public double getCompletionPercentage() {
        return totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
    }
    
    // Nested class for care task summaries
    public static class CareTaskSummary {
        private UUID id;
        private String taskName;
        private String category;
        private String priority;
        private String frequency;
        private boolean isCompleted;
        private boolean isOverdue;
        private LocalDateTime nextDueDate;
        
        public CareTaskSummary() {}
        
        public CareTaskSummary(UUID id, String taskName, String category, String priority, 
                               String frequency, boolean isCompleted, boolean isOverdue, LocalDateTime nextDueDate) {
            this.id = id;
            this.taskName = taskName;
            this.category = category;
            this.priority = priority;
            this.frequency = frequency;
            this.isCompleted = isCompleted;
            this.isOverdue = isOverdue;
            this.nextDueDate = nextDueDate;
        }
        
        // Getters and setters
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
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
        public boolean isOverdue() { return isOverdue; }
        public void setOverdue(boolean overdue) { isOverdue = overdue; }
        public LocalDateTime getNextDueDate() { return nextDueDate; }
        public void setNextDueDate(LocalDateTime nextDueDate) { this.nextDueDate = nextDueDate; }
    }
}