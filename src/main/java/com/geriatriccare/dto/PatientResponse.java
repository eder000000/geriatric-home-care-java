package com.geriatriccare.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.geriatriccare.dto.summary.CaregiverSummary;
import com.geriatriccare.dto.summary.FamilyMemberSummary;

public class PatientResponse {
    

    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String medicalConditions;
    private String emergencyContact;
    private String emergencyPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private int age;
    private String fullName;

    // Relationship summaries (not full objects for security)
    private List<CaregiverSummary> caregivers;
    private List<FamilyMemberSummary> familyMembers;
    private CaregiverSummary primaryCaregiver;

    //Constructors
    public PatientResponse() {}
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

     public List<CaregiverSummary> getCaregivers() { 
        return caregivers; 
    }
    
    public void setCaregivers(List<CaregiverSummary> caregivers) { 
        this.caregivers = caregivers; 
    }
    
    public List<FamilyMemberSummary> getFamilyMembers() { 
        return familyMembers; 
    }
    
    public void setFamilyMembers(List<FamilyMemberSummary> familyMembers) { 
        this.familyMembers = familyMembers; 
    }
    
    public CaregiverSummary getPrimaryCaregiver() { 
        return primaryCaregiver; 
    }
    
    public void setPrimaryCaregiver(CaregiverSummary primaryCaregiver) { 
        this.primaryCaregiver = primaryCaregiver; 
    }
}