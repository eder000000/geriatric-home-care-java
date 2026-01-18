package com.geriatriccare.dto.security;

import jakarta.validation.constraints.NotBlank;

public class EncryptionRequest {
    
    @NotBlank(message = "Data to encrypt is required")
    private String data;
    
    private EncryptionAlgorithm algorithm;
    private String context;
    
    public EncryptionRequest() {
        this.algorithm = EncryptionAlgorithm.AES_256_GCM;
    }
    
    // Getters and Setters
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public EncryptionAlgorithm getAlgorithm() { return algorithm; }
    public void setAlgorithm(EncryptionAlgorithm algorithm) { this.algorithm = algorithm; }
    
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}
