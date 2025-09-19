package com.geriatriccare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "patients")
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "First name is required")
    @Size(max = 255, message = "First name must not exceed 255 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 255, message = "Last name must not exceed 255 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    
    @Column(name = "medical_conditions", columnDefinition = "TEXT")
    private String medicalConditions;
    
    @Size(max = 255, message = "Emergency contact name must not exceed 255 characters")
    @Column(name = "emergency_contact")
    private String emergencyContact;
    
    @Size(max = 255, message = "Emergency phone must not exceed 255 characters")
    @Column(name = "emergency_phone")
    private String emergencyPhone;
    
    // Many-to-Many relationship with caregivers through junction table
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PatientCaregiver> patientCaregivers = new ArrayList<>();
    
    // One-to-Many relationship with family members (max 2 active)
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PatientFamilyMember> patientFamilyMembers = new ArrayList<>();
    
    // Care plans relationship
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarePlan> carePlans = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public Patient() {}
    
    public Patient(String firstName, String lastName, LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getMedicalConditions() {
        return medicalConditions;
    }
    
    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }
    
    public String getEmergencyContact() {
        return emergencyContact;
    }
    
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
    
    public String getEmergencyPhone() {
        return emergencyPhone;
    }
    
    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }
    
    public List<PatientCaregiver> getPatientCaregivers() {
        return patientCaregivers;
    }
    
    public void setPatientCaregivers(List<PatientCaregiver> patientCaregivers) {
        this.patientCaregivers = patientCaregivers;
    }
    
    public List<PatientFamilyMember> getPatientFamilyMembers() {
        return patientFamilyMembers;
    }
    
    public void setPatientFamilyMembers(List<PatientFamilyMember> patientFamilyMembers) {
        this.patientFamilyMembers = patientFamilyMembers;
    }
    
    public List<CarePlan> getCarePlans() {
        return carePlans;
    }
    
    public void setCarePlans(List<CarePlan> carePlans) {
        this.carePlans = carePlans;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Helper methods for caregivers
    public List<User> getActiveCaregivers() {
        return patientCaregivers.stream()
                .filter(pc -> pc.getIsActive())
                .map(PatientCaregiver::getCaregiver)
                .collect(Collectors.toList());
    }
    
    public User getPrimaryCaregiver() {
        return patientCaregivers.stream()
                .filter(pc -> pc.getIsActive() && pc.getIsPrimary())
                .map(PatientCaregiver::getCaregiver)
                .findFirst()
                .orElse(null);
    }
    
    public void assignCaregiver(User caregiver, User assignedBy, boolean isPrimary) {
        // If setting as primary, unset other primary caregivers
        if (isPrimary) {
            patientCaregivers.stream()
                    .filter(pc -> pc.getIsActive() && pc.getIsPrimary())
                    .forEach(pc -> pc.setIsPrimary(false));
        }
        
        PatientCaregiver patientCaregiver = new PatientCaregiver(this, caregiver, assignedBy);
        patientCaregiver.setIsPrimary(isPrimary);
        patientCaregivers.add(patientCaregiver);
    }
    
    public void removeCaregiver(User caregiver) {
        patientCaregivers.stream()
                .filter(pc -> pc.getCaregiver().equals(caregiver) && pc.getIsActive())
                .forEach(PatientCaregiver::deactivate);
    }
    
    // Helper methods for family members
    public List<User> getActiveFamilyMembers() {
        return patientFamilyMembers.stream()
                .filter(pfm -> pfm.getIsActive())
                .map(PatientFamilyMember::getFamilyMember)
                .collect(Collectors.toList());
    }
    
    public boolean canAssignFamilyMember() {
        long activeCount = patientFamilyMembers.stream()
                .filter(pfm -> pfm.getIsActive())
                .count();
        return activeCount < 2; // Maximum 2 active family members
    }
    
    public void assignFamilyMember(User familyMember, User assignedBy) {
        if (!canAssignFamilyMember()) {
            throw new IllegalStateException("Patient already has maximum number of family members (2)");
        }
        
        PatientFamilyMember patientFamilyMember = new PatientFamilyMember(this, familyMember, assignedBy);
        patientFamilyMembers.add(patientFamilyMember);
    }
    
    public void removeFamilyMember(User familyMember) {
        patientFamilyMembers.stream()
                .filter(pfm -> pfm.getFamilyMember().equals(familyMember) && pfm.getIsActive())
                .forEach(PatientFamilyMember::deactivate);
    }
    
    // General helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public void addCarePlan(CarePlan carePlan) {
        carePlans.add(carePlan);
        carePlan.setPatient(this);
    }
    
    public void removeCarePlan(CarePlan carePlan) {
        carePlans.remove(carePlan);
        carePlan.setPatient(null);
    }
    
    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
    
    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", isActive=" + isActive +
                '}';
    }
}