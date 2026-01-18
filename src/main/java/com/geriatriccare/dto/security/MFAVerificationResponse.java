package com.geriatriccare.dto.security;

public class MFAVerificationResponse {
    
    private Boolean verified;
    private String message;
    private String token; // JWT token if verification successful
    private Integer remainingAttempts;
    
    public MFAVerificationResponse() {}
    
    public MFAVerificationResponse(Boolean verified, String message) {
        this.verified = verified;
        this.message = message;
    }
    
    // Getters and Setters
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public Integer getRemainingAttempts() { return remainingAttempts; }
    public void setRemainingAttempts(Integer remainingAttempts) { this.remainingAttempts = remainingAttempts; }
}
