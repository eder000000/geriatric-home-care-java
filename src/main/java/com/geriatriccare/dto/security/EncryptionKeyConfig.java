package com.geriatriccare.dto.security;

import java.time.LocalDateTime;

public class EncryptionKeyConfig {
    
    private String keyId;
    private Integer keyVersion;
    private EncryptionAlgorithm algorithm;
    private LocalDateTime createdAt;
    private LocalDateTime rotatedAt;
    private Boolean isActive;
    private String description;
    
    public EncryptionKeyConfig() {
        this.keyVersion = 1;
        this.algorithm = EncryptionAlgorithm.AES_256_GCM;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Getters and Setters
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    
    public Integer getKeyVersion() { return keyVersion; }
    public void setKeyVersion(Integer keyVersion) { this.keyVersion = keyVersion; }
    
    public EncryptionAlgorithm getAlgorithm() { return algorithm; }
    public void setAlgorithm(EncryptionAlgorithm algorithm) { this.algorithm = algorithm; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getRotatedAt() { return rotatedAt; }
    public void setRotatedAt(LocalDateTime rotatedAt) { this.rotatedAt = rotatedAt; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
