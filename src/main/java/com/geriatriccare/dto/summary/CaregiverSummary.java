package com.geriatriccare.dto.summary;

import java.util.UUID;

public class CaregiverSummary {

    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private boolean isPrimary;
    
    //constructors
    public CaregiverSummary() {}

    public CaregiverSummary(UUID id, String firstName, String lastName, String email, boolean isPrimary) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.email = email;
        this.isPrimary = isPrimary;
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
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public boolean isPrimary() {
        return isPrimary;
    }
    public void setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}   
