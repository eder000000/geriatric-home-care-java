package com.geriatriccare.dto.security;

import jakarta.validation.constraints.NotBlank;

public class PasswordValidationRequest {
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private String username;
    
    public PasswordValidationRequest() {}
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
