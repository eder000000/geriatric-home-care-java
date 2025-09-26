package com.geriatriccare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "care_plan_templates")
public class CarePlanTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarePlanPriority defaultPriority;
    
    @Size(max = 100)
    private String category; // e.g., "Diabetes Care", "Post-Surgery", "Dementia Care"
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CareTaskTemplate> taskTemplates = new ArrayList<>();
    
    // Constructors
    public CarePlanTemplate() {}
    
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
    
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    
    public List<CareTaskTemplate> getTaskTemplates() { return taskTemplates; }
    public void setTaskTemplates(List<CareTaskTemplate> taskTemplates) { 
        this.taskTemplates = taskTemplates; 
    }
    
    // Helper methods
    public void addTaskTemplate(CareTaskTemplate taskTemplate) {
        taskTemplates.add(taskTemplate);
        taskTemplate.setTemplate(this);
    }
    
    public void removeTaskTemplate(CareTaskTemplate taskTemplate) {
        taskTemplates.remove(taskTemplate);
        taskTemplate.setTemplate(null);
    }
}