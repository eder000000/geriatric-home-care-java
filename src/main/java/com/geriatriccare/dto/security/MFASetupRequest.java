package com.geriatriccare.dto.security;

import jakarta.validation.constraints.NotNull;

public class MFASetupRequest {
    
    @NotNull(message = "MFA method is required")
    private MFAMethod method;
    
    private String phoneNumber; // For SMS
    private String email; // For EMAIL
    
    public MFASetupRequest() {}
    
    // Getters and Setters
    public MFAMethod getMethod() { return method; }
    public void setMethod(MFAMethod method) { this.method = method; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
