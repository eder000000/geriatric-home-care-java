package com.geriatriccare.dto.security;

import java.util.List;

public class MFASetupResponse {
    
    private MFAMethod method;
    private String qrCodeUrl; // For TOTP
    private String secretKey; // For TOTP
    private List<String> backupCodes;
    private String message;
    private Boolean setupComplete;
    
    public MFASetupResponse() {
        this.setupComplete = false;
    }
    
    // Getters and Setters
    public MFAMethod getMethod() { return method; }
    public void setMethod(MFAMethod method) { this.method = method; }
    
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    
    public List<String> getBackupCodes() { return backupCodes; }
    public void setBackupCodes(List<String> backupCodes) { this.backupCodes = backupCodes; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Boolean getSetupComplete() { return setupComplete; }
    public void setSetupComplete(Boolean setupComplete) { this.setupComplete = setupComplete; }
}
