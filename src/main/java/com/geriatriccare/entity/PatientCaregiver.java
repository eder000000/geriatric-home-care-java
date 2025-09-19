package com.geriatriccare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "patient_caregivers")
@IdClass(PatientCaregiver.PatientCaregiverId.class)
public class PatientCaregiver {
    
    @Id
    @Column(name = "patient_id")
    private UUID patientId;
    
    @Id
    @Column(name = "caregiver_id")
    private UUID caregiverId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id", insertable = false, updatable = false)
    private User caregiver;
    
    @NotNull(message = "Assigned by user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;
    
    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public PatientCaregiver() {}
    
    public PatientCaregiver(UUID patientId, UUID caregiverId, User assignedBy) {
        this.patientId = patientId;
        this.caregiverId = caregiverId;
        this.assignedBy = assignedBy;
    }
    
    public PatientCaregiver(Patient patient, User caregiver, User assignedBy) {
        this.patient = patient;
        this.caregiver = caregiver;
        this.patientId = patient.getId();
        this.caregiverId = caregiver.getId();
        this.assignedBy = assignedBy;
    }
    
    // Getters and Setters
    public UUID getPatientId() {
        return patientId;
    }
    
    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }
    
    public UUID getCaregiverId() {
        return caregiverId;
    }
    
    public void setCaregiverId(UUID caregiverId) {
        this.caregiverId = caregiverId;
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patientId = patient.getId();
        }
    }
    
    public User getCaregiver() {
        return caregiver;
    }
    
    public void setCaregiver(User caregiver) {
        this.caregiver = caregiver;
        if (caregiver != null) {
            this.caregiverId = caregiver.getId();
        }
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
    
    public Boolean getIsPrimary() {
        return isPrimary;
    }
    
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientCaregiver that = (PatientCaregiver) o;
        return Objects.equals(patientId, that.patientId) && 
               Objects.equals(caregiverId, that.caregiverId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(patientId, caregiverId);
    }
    
    @Override
    public String toString() {
        return "PatientCaregiver{" +
                "patientId=" + patientId +
                ", caregiverId=" + caregiverId +
                ", isPrimary=" + isPrimary +
                ", isActive=" + isActive +
                ", assignedAt=" + assignedAt +
                '}';
    }
    
    // Composite ID class
    public static class PatientCaregiverId implements Serializable {
        private UUID patientId;
        private UUID caregiverId;
        
        public PatientCaregiverId() {}
        
        public PatientCaregiverId(UUID patientId, UUID caregiverId) {
            this.patientId = patientId;
            this.caregiverId = caregiverId;
        }
        
        // Getters and Setters
        public UUID getPatientId() {
            return patientId;
        }
        
        public void setPatientId(UUID patientId) {
            this.patientId = patientId;
        }
        
        public UUID getCaregiverId() {
            return caregiverId;
        }
        
        public void setCaregiverId(UUID caregiverId) {
            this.caregiverId = caregiverId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PatientCaregiverId that = (PatientCaregiverId) o;
            return Objects.equals(patientId, that.patientId) && 
                   Objects.equals(caregiverId, that.caregiverId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(patientId, caregiverId);
        }
    }
}