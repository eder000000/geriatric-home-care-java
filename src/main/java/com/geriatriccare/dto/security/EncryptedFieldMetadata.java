package com.geriatriccare.dto.security;

public class EncryptedFieldMetadata {
    
    private String fieldName;
    private EncryptionAlgorithm algorithm;
    private Integer keyVersion;
    private String ivBase64;
    private Boolean isEncrypted;
    
    public EncryptedFieldMetadata() {
        this.isEncrypted = false;
    }
    
    // Getters and Setters
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    
    public EncryptionAlgorithm getAlgorithm() { return algorithm; }
    public void setAlgorithm(EncryptionAlgorithm algorithm) { this.algorithm = algorithm; }
    
    public Integer getKeyVersion() { return keyVersion; }
    public void setKeyVersion(Integer keyVersion) { this.keyVersion = keyVersion; }
    
    public String getIvBase64() { return ivBase64; }
    public void setIvBase64(String ivBase64) { this.ivBase64 = ivBase64; }
    
    public Boolean getIsEncrypted() { return isEncrypted; }
    public void setIsEncrypted(Boolean isEncrypted) { this.isEncrypted = isEncrypted; }
}
