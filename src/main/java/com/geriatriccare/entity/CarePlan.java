package com.geriatriccare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "care_plans")
@EntityListeners(AuditingEntityListener.class)
public class CarePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @NotBlank(message = "Care plan title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    @NotNull(message = "Creator is required")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_caregiver_id")
    private User assignedCaregiver;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarePlanStatus status = CarePlanStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarePlanPriority priority = CarePlanPriority.MEDIUM;

    @OneToMany(mappedBy = "carePlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CareTask> careTasks = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CarePlan() {}

    public CarePlan(String title, Patient patient, User createdBy, LocalDate startDate) {
        this.title = title;
        this.patient = patient;
        this.createdBy = createdBy;
        this.startDate = startDate;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getAssignedCaregiver() { return assignedCaregiver; }
    public void setAssignedCaregiver(User assignedCaregiver) { this.assignedCaregiver = assignedCaregiver; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public CarePlanStatus getStatus() { return status; }
    public void setStatus(CarePlanStatus status) { this.status = status; }

    public CarePlanPriority getPriority() { return priority; }
    public void setPriority(CarePlanPriority priority) { this.priority = priority; }

    public List<CareTask> getCareTasks() { return careTasks; }
    public void setCareTasks(List<CareTask> careTasks) { this.careTasks = careTasks; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business methods
    public boolean isCurrentlyActive() {
        return isActive && status == CarePlanStatus.ACTIVE && 
               (endDate == null || endDate.isAfter(LocalDate.now()));
    }

    public void addCareTask(CareTask task) {
        careTasks.add(task);
        task.setCarePlan(this);
    }

    public void removeCareTask(CareTask task) {
        careTasks.remove(task);
        task.setCarePlan(null);
    }
}