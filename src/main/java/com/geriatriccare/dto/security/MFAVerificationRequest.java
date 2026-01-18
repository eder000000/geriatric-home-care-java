package com.geriatriccare.dto.security;

import jakarta.validation.constraints.NotBlank;

public class MFAVerificationRequest {
    
    @NotBlank(message = "Verification code is required")
    private String code;
    
    private Boolean trustDevice;
    
    public MFAVerificationRequest() {
        this.trustDevice = false;
    }
    
    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public Boolean getTrustDevice() { return trustDevice; }
    public void setTrustDevice(Boolean trustDevice) { this.trustDevice = trustDevice; }
}
