package com.geriatriccare.dto.security;

public enum EncryptionAlgorithm {
    AES_256_GCM("AES/GCM/NoPadding", 256, "AES-256-GCM encryption"),
    AES_128_GCM("AES/GCM/NoPadding", 128, "AES-128-GCM encryption");
    
    private final String transformation;
    private final int keySize;
    private final String description;
    
    EncryptionAlgorithm(String transformation, int keySize, String description) {
        this.transformation = transformation;
        this.keySize = keySize;
        this.description = description;
    }
    
    public String getTransformation() { return transformation; }
    public int getKeySize() { return keySize; }
    public String getDescription() { return description; }
}
