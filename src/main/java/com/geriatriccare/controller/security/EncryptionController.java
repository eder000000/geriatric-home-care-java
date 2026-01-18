package com.geriatriccare.controller.security;

import com.geriatriccare.dto.security.*;
import com.geriatriccare.service.security.EncryptionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/encryption")
public class EncryptionController {
    
    private static final Logger logger = LoggerFactory.getLogger(EncryptionController.class);
    
    private final EncryptionService encryptionService;
    
    @Autowired
    public EncryptionController(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }
    
    @PostMapping("/encrypt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EncryptionResponse> encryptData(@Valid @RequestBody EncryptionRequest request) {
        
        logger.info("Manual encryption request");
        
        EncryptionResponse response = encryptionService.encryptData(request);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/decrypt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> decryptData(
            @RequestParam String encryptedData,
            @RequestParam(required = false) Integer keyVersion) {
        
        logger.info("Manual decryption request");
        
        try {
            String decrypted = encryptionService.decryptData(encryptedData, keyVersion);
            return ResponseEntity.ok(decrypted);
        } catch (RuntimeException e) {
            logger.error("Decryption failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Decryption failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/rotate-keys")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rotateKeys(@RequestBody KeyRotationRequest request) {
        
        logger.info("Key rotation request");
        
        Map<String, Object> result = encryptionService.rotateKeys(request);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @GetMapping("/key-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getKeyInfo() {
        
        logger.info("Encryption key info request");
        
        Map<String, Object> info = encryptionService.getKeyInfo();
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "Encryption Service",
                "currentKeyVersion", encryptionService.getCurrentKeyVersion()
        );
        return ResponseEntity.ok(health);
    }
}
