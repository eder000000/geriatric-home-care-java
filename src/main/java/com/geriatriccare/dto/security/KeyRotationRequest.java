package com.geriatriccare.dto.security;

public class KeyRotationRequest {
    
    private EncryptionAlgorithm newAlgorithm;
    private Boolean archiveOldKeys;
    private String reason;
    
    public KeyRotationRequest() {
        this.newAlgorithm = EncryptionAlgorithm.AES_256_GCM;
        this.archiveOldKeys = true;
    }
    
    // Getters and Setters
    public EncryptionAlgorithm getNewAlgorithm() { return newAlgorithm; }
    public void setNewAlgorithm(EncryptionAlgorithm newAlgorithm) { this.newAlgorithm = newAlgorithm; }
    
    public Boolean getArchiveOldKeys() { return archiveOldKeys; }
    public void setArchiveOldKeys(Boolean archiveOldKeys) { this.archiveOldKeys = archiveOldKeys; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
