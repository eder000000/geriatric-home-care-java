package com.geriatriccare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient_family_members", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id", "family_member_id"}))
public class PatientFamilyMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "Patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @NotNull(message = "Family member is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id", nullable = false)
    private User familyMember;
    
    @NotNull(message = "Assigned by user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;
    
    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public PatientFamilyMember() {}
    
    public PatientFamilyMember(Patient patient, User familyMember, User assignedBy) {
        this.patient = patient;
        this.familyMember = familyMember;
        this.assignedBy = assignedBy;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public User getFamilyMember() {
        return familyMember;
    }
    
    public void setFamilyMember(User familyMember) {
        this.familyMember = familyMember;
    }
    
    public User getAssignedBy() {
        return assignedBy;
    }
    
    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    @Override
    public String toString() {
        return "PatientFamilyMember{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getId() : null) +
                ", familyMember=" + (familyMember != null ? familyMember.getId() : null) +
                ", isActive=" + isActive +
                ", assignedAt=" + assignedAt +
                '}';
    }
}