package com.geriatriccare.service.security;

import com.geriatriccare.dto.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String AES = "AES";
    
    private final Map<Integer, SecretKey> keyStore = new ConcurrentHashMap<>();
    private final Map<Integer, EncryptionKeyConfig> keyConfigs = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    
    private int currentKeyVersion = 1;
    
    public EncryptionService() {
        initializeKeys();
    }
    
    public String encrypt(String plaintext) throws Exception {
        return encrypt(plaintext, currentKeyVersion);
    }
    
    public String encrypt(String plaintext, int keyVersion) throws Exception {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        
        logger.debug("Encrypting data with key version: {}", keyVersion);
        
        SecretKey key = keyStore.get(keyVersion);
        if (key == null) {
            throw new IllegalArgumentException("Invalid key version: " + keyVersion);
        }
        
        byte[] iv = generateIV();
        
        Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES_256_GCM.getTransformation());
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }
    
    public String decrypt(String encryptedData) throws Exception {
        return decrypt(encryptedData, currentKeyVersion);
    }
    
    public String decrypt(String encryptedData, int keyVersion) throws Exception {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        
        logger.debug("Decrypting data with key version: {}", keyVersion);
        
        SecretKey key = keyStore.get(keyVersion);
        if (key == null) {
            throw new IllegalArgumentException("Invalid key version: " + keyVersion);
        }
        
        byte[] combined = Base64.getDecoder().decode(encryptedData);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] encryptedBytes = new byte[combined.length - GCM_IV_LENGTH];
        
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);
        
        Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES_256_GCM.getTransformation());
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        
        return new String(decryptedBytes);
    }
    
    public EncryptionResponse encryptData(EncryptionRequest request) {
        EncryptionResponse response = new EncryptionResponse();
        
        try {
            String encrypted = encrypt(request.getData());
            
            response.setEncryptedData(encrypted);
            response.setKeyVersion(currentKeyVersion);
            response.setAlgorithm(EncryptionAlgorithm.AES_256_GCM);
            response.setSuccess(true);
            response.setMessage("Data encrypted successfully");
            
            logger.info("Data encrypted successfully");
            
        } catch (Exception e) {
            logger.error("Encryption failed: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("Encryption failed: " + e.getMessage());
        }
        
        return response;
    }
    
    public String decryptData(String encryptedData, Integer keyVersion) {
        try {
            int version = keyVersion != null ? keyVersion : currentKeyVersion;
            return decrypt(encryptedData, version);
        } catch (Exception e) {
            logger.error("Decryption failed: {}", e.getMessage());
            throw new RuntimeException("Decryption failed: " + e.getMessage());
        }
    }
    
    public Map<String, Object> rotateKeys(KeyRotationRequest request) {
        logger.info("Starting key rotation. Reason: {}", request.getReason());
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            int oldVersion = currentKeyVersion;
            int newVersion = currentKeyVersion + 1;
            
            SecretKey newKey = generateKey(request.getNewAlgorithm());
            
            keyStore.put(newVersion, newKey);
            
            EncryptionKeyConfig config = new EncryptionKeyConfig();
            config.setKeyId("key-" + newVersion);
            config.setKeyVersion(newVersion);
            config.setAlgorithm(request.getNewAlgorithm());
            config.setDescription(request.getReason());
            
            keyConfigs.put(newVersion, config);
            
            if (request.getArchiveOldKeys()) {
                EncryptionKeyConfig oldConfig = keyConfigs.get(oldVersion);
                if (oldConfig != null) {
                    oldConfig.setIsActive(false);
                    oldConfig.setRotatedAt(LocalDateTime.now());
                }
            }
            
            currentKeyVersion = newVersion;
            
            result.put("success", true);
            result.put("oldKeyVersion", oldVersion);
            result.put("newKeyVersion", newVersion);
            result.put("algorithm", request.getNewAlgorithm().name());
            result.put("message", "Key rotation completed successfully");
            
            logger.info("Key rotation completed: v{} -> v{}", oldVersion, newVersion);
            
        } catch (Exception e) {
            logger.error("Key rotation failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Key rotation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    public Map<String, Object> getKeyInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("currentKeyVersion", currentKeyVersion);
        info.put("totalKeys", keyStore.size());
        info.put("activeKeys", keyConfigs.values().stream()
                .filter(EncryptionKeyConfig::getIsActive)
                .count());
        
        List<Map<String, Object>> keys = new ArrayList<>();
        for (Map.Entry<Integer, EncryptionKeyConfig> entry : keyConfigs.entrySet()) {
            Map<String, Object> keyInfo = new HashMap<>();
            EncryptionKeyConfig config = entry.getValue();
            
            keyInfo.put("version", entry.getKey());
            keyInfo.put("algorithm", config.getAlgorithm().name());
            keyInfo.put("isActive", config.getIsActive());
            keyInfo.put("createdAt", config.getCreatedAt());
            keyInfo.put("rotatedAt", config.getRotatedAt());
            
            keys.add(keyInfo);
        }
        
        info.put("keys", keys);
        
        return info;
    }
    
    public List<String> encryptBatch(List<String> plaintexts) {
        return plaintexts.stream()
                .map(plaintext -> {
                    try {
                        return encrypt(plaintext);
                    } catch (Exception e) {
                        logger.error("Batch encryption error: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
    
    public List<String> decryptBatch(List<String> encryptedData) {
        return encryptedData.stream()
                .map(encrypted -> {
                    try {
                        return decrypt(encrypted);
                    } catch (Exception e) {
                        logger.error("Batch decryption error: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
    
    private void initializeKeys() {
        try {
            logger.info("Initializing encryption keys");
            
            SecretKey key = generateKey(EncryptionAlgorithm.AES_256_GCM);
            keyStore.put(currentKeyVersion, key);
            
            EncryptionKeyConfig config = new EncryptionKeyConfig();
            config.setKeyId("key-" + currentKeyVersion);
            config.setKeyVersion(currentKeyVersion);
            config.setDescription("Initial encryption key");
            
            keyConfigs.put(currentKeyVersion, config);
            
            logger.info("Encryption keys initialized. Current version: {}", currentKeyVersion);
            
        } catch (Exception e) {
            logger.error("Failed to initialize encryption keys: {}", e.getMessage());
            throw new RuntimeException("Encryption initialization failed", e);
        }
    }
    
    private SecretKey generateKey(EncryptionAlgorithm algorithm) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
        keyGenerator.init(algorithm.getKeySize(), secureRandom);
        return keyGenerator.generateKey();
    }
    
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }
    
    public int getCurrentKeyVersion() {
        return currentKeyVersion;
    }
}
