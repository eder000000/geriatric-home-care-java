package com.geriatriccare.service.security;

import com.geriatriccare.dto.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MFAService {
    
    private static final Logger log = LoggerFactory.getLogger(MFAService.class);
    private static final int CODE_LENGTH = 6;
    private static final int CODE_VALIDITY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    
    // In-memory storage (use database in production)
    private final Map<String, MFASession> sessions = new ConcurrentHashMap<>();
    private final Map<String, UserMFAConfig> userConfigs = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    
    public MFASetupResponse setupMFA(MFASetupRequest request) {
        String userId = getCurrentUserId();
        log.info("Setting up MFA for user: {} with method: {}", userId, request.getMethod());
        
        MFASetupResponse response = new MFASetupResponse();
        response.setMethod(request.getMethod());
        
        switch (request.getMethod()) {
            case TOTP:
                String secret = generateSecret();
                response.setSecretKey(secret);
                response.setQrCodeUrl(generateQRCodeUrl(userId, secret));
                response.setMessage("Scan QR code with authenticator app");
                break;
                
            case SMS:
                if (request.getPhoneNumber() == null) {
                    throw new RuntimeException("Phone number required for SMS");
                }
                response.setMessage("SMS verification code will be sent to: " + maskPhone(request.getPhoneNumber()));
                break;
                
            case EMAIL:
                if (request.getEmail() == null) {
                    throw new RuntimeException("Email required for email verification");
                }
                response.setMessage("Verification code will be sent to: " + maskEmail(request.getEmail()));
                break;
                
            case BACKUP_CODES:
                List<String> backupCodes = generateBackupCodes(10);
                response.setBackupCodes(backupCodes);
                response.setMessage("Save these backup codes securely");
                break;
        }
        
        // Store config
        UserMFAConfig config = new UserMFAConfig();
        config.setUserId(userId);
        config.setMethod(request.getMethod());
        config.setStatus(MFAStatus.PENDING_SETUP);
        config.setSetupDate(LocalDateTime.now());
        config.setPhoneNumber(request.getPhoneNumber());
        config.setEmail(request.getEmail());
        config.setSecretKey(response.getSecretKey());
        config.setBackupCodes(response.getBackupCodes());
        
        userConfigs.put(userId, config);
        
        log.info("MFA setup initiated for user: {}", userId);
        return response;
    }
    
    public MFAVerificationResponse sendVerificationCode() {
        String userId = getCurrentUserId();
        UserMFAConfig config = userConfigs.get(userId);
        
        if (config == null) {
            return new MFAVerificationResponse(false, "MFA not configured");
        }
        
        String code = generateCode();
        
        MFASession session = new MFASession();
        session.setUserId(userId);
        session.setCode(code);
        session.setMethod(config.getMethod());
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES));
        session.setAttempts(0);
        
        sessions.put(userId, session);
        
        // Simulate sending code (in production: integrate with SMS/Email service)
        log.info("MFA code generated for user: {} (method: {})", userId, config.getMethod());
        log.debug("CODE FOR TESTING: {}", code); // Remove in production!
        
        return new MFAVerificationResponse(true, 
            "Verification code sent via " + config.getMethod().getDisplayName());
    }
    
    public MFAVerificationResponse verifyCode(MFAVerificationRequest request) {
        String userId = getCurrentUserId();
        MFASession session = sessions.get(userId);
        
        if (session == null) {
            return new MFAVerificationResponse(false, "No active MFA session");
        }
        
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            sessions.remove(userId);
            return new MFAVerificationResponse(false, "Code expired");
        }
        
        session.setAttempts(session.getAttempts() + 1);
        
        if (session.getAttempts() > MAX_ATTEMPTS) {
            sessions.remove(userId);
            return new MFAVerificationResponse(false, "Maximum attempts exceeded");
        }
        
        if (session.getCode().equals(request.getCode())) {
            sessions.remove(userId);
            
            // Update config to enabled
            UserMFAConfig config = userConfigs.get(userId);
            if (config != null) {
                config.setStatus(MFAStatus.ENABLED);
                config.setLastVerified(LocalDateTime.now());
            }
            
            log.info("MFA verification successful for user: {}", userId);
            
            MFAVerificationResponse response = new MFAVerificationResponse(true, "Verification successful");
            // In production: generate new JWT token here
            response.setToken("jwt-token-placeholder");
            return response;
        }
        
        MFAVerificationResponse response = new MFAVerificationResponse(false, "Invalid code");
        response.setRemainingAttempts(MAX_ATTEMPTS - session.getAttempts());
        return response;
    }
    
    public void enableMFA(String userId) {
        log.info("Enabling MFA for user: {}", userId);
        UserMFAConfig config = userConfigs.get(userId);
        if (config != null) {
            config.setStatus(MFAStatus.ENABLED);
        }
    }
    
    public void disableMFA(String userId) {
        log.info("Disabling MFA for user: {}", userId);
        UserMFAConfig config = userConfigs.get(userId);
        if (config != null) {
            config.setStatus(MFAStatus.DISABLED);
        }
    }
    
    public MFAStatus getMFAStatus(String userId) {
        UserMFAConfig config = userConfigs.get(userId);
        return config != null ? config.getStatus() : MFAStatus.DISABLED;
    }
    
    public Boolean isMFAEnabled(String userId) {
        return getMFAStatus(userId) == MFAStatus.ENABLED;
    }
    
    // Helper methods
    private String generateCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    private String generateSecret() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    private String generateQRCodeUrl(String userId, String secret) {
        String issuer = "GeriatricCare";
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", 
                           issuer, userId, secret, issuer);
    }
    
    private List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            codes.add(String.format("%08d", random.nextInt(100000000)));
        }
        return codes;
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return "***-***-" + phone.substring(phone.length() - 4);
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        return parts[0].substring(0, 2) + "***@" + parts[1];
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "unknown";
    }
    
    // Inner classes for storage
    private static class MFASession {
        private String userId;
        private String code;
        private MFAMethod method;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private Integer attempts;
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public MFAMethod getMethod() { return method; }
        public void setMethod(MFAMethod method) { this.method = method; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        public Integer getAttempts() { return attempts; }
        public void setAttempts(Integer attempts) { this.attempts = attempts; }
    }
    
    private static class UserMFAConfig {
        private String userId;
        private MFAMethod method;
        private MFAStatus status;
        private String phoneNumber;
        private String email;
        private String secretKey;
        private List<String> backupCodes;
        private LocalDateTime setupDate;
        private LocalDateTime lastVerified;
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public MFAMethod getMethod() { return method; }
        public void setMethod(MFAMethod method) { this.method = method; }
        public MFAStatus getStatus() { return status; }
        public void setStatus(MFAStatus status) { this.status = status; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public List<String> getBackupCodes() { return backupCodes; }
        public void setBackupCodes(List<String> backupCodes) { this.backupCodes = backupCodes; }
        public LocalDateTime getSetupDate() { return setupDate; }
        public void setSetupDate(LocalDateTime setupDate) { this.setupDate = setupDate; }
        public LocalDateTime getLastVerified() { return lastVerified; }
        public void setLastVerified(LocalDateTime lastVerified) { this.lastVerified = lastVerified; }
    }
}
