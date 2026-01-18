package com.geriatriccare.service.security;

import com.geriatriccare.dto.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class PasswordPolicyService {
    
    private static final Logger log = LoggerFactory.getLogger(PasswordPolicyService.class);
    
    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 128;
    private static final int PASSWORD_HISTORY_SIZE = 5;
    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>(Arrays.asList(
        "password", "123456", "12345678", "qwerty", "abc123", "monkey", "letmein",
        "trustno1", "dragon", "baseball", "iloveyou", "master", "sunshine",
        "ashley", "bailey", "passw0rd", "shadow", "123123", "654321", "superman"
    ));
    
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();
    
    private final Map<String, List<String>> passwordHistory = new ConcurrentHashMap<>();
    private final Map<String, PasswordMetadata> userPasswordMetadata = new ConcurrentHashMap<>();
    private final Map<String, LoginAttempts> loginAttempts = new ConcurrentHashMap<>();
    
    public PasswordValidationResponse validatePassword(PasswordValidationRequest request) {
        log.info("Validating password strength");
        
        PasswordValidationResponse response = new PasswordValidationResponse();
        String password = request.getPassword();
        
        if (password.length() < MIN_LENGTH) {
            response.getViolations().add("Password must be at least " + MIN_LENGTH + " characters long");
        }
        
        if (password.length() > MAX_LENGTH) {
            response.getViolations().add("Password must not exceed " + MAX_LENGTH + " characters");
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            response.getViolations().add("Password must contain at least one uppercase letter");
            response.getSuggestions().add("Add an uppercase letter (A-Z)");
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            response.getViolations().add("Password must contain at least one lowercase letter");
            response.getSuggestions().add("Add a lowercase letter (a-z)");
        }
        
        if (!DIGIT_PATTERN.matcher(password).find()) {
            response.getViolations().add("Password must contain at least one number");
            response.getSuggestions().add("Add a number (0-9)");
        }
        
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            response.getViolations().add("Password must contain at least one special character");
            response.getSuggestions().add("Add a special character (!@#$%^&*)");
        }
        
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            if (password.toLowerCase().contains(request.getUsername().toLowerCase())) {
                response.getViolations().add("Password must not contain your username");
            }
        }
        
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            response.getViolations().add("This password is too common and easily guessed");
            response.getSuggestions().add("Use a unique, complex password");
        }
        
        int score = calculatePasswordStrength(password);
        response.setScore(score);
        response.setStrength(getPasswordStrengthLevel(score));
        
        response.setIsValid(response.getViolations().isEmpty() && score >= 3);
        
        if (response.getIsValid()) {
            response.setMessage("Password meets all requirements");
        } else {
            response.setMessage("Password does not meet security requirements");
        }
        
        log.info("Password validation complete - Strength: {}, Valid: {}", 
                 response.getStrength(), response.getIsValid());
        
        return response;
    }
    
    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        log.info("Processing password change for user: {}", userId);
        
        PasswordValidationRequest request = new PasswordValidationRequest();
        request.setPassword(newPassword);
        request.setUsername(userId);
        
        PasswordValidationResponse validation = validatePassword(request);
        if (!validation.getIsValid()) {
            throw new RuntimeException("New password does not meet requirements: " + 
                                     String.join(", ", validation.getViolations()));
        }
        
        List<String> history = passwordHistory.getOrDefault(userId, new ArrayList<>());
        for (String oldPasswordHash : history) {
            if (passwordEncoder.matches(newPassword, oldPasswordHash)) {
                throw new RuntimeException("Cannot reuse any of your last " + 
                                         PASSWORD_HISTORY_SIZE + " passwords");
            }
        }
        
        String newPasswordHash = passwordEncoder.encode(newPassword);
        
        history.add(0, newPasswordHash);
        if (history.size() > PASSWORD_HISTORY_SIZE) {
            history = history.subList(0, PASSWORD_HISTORY_SIZE);
        }
        passwordHistory.put(userId, history);
        
        PasswordMetadata metadata = new PasswordMetadata();
        metadata.setUserId(userId);
        metadata.setLastChanged(LocalDateTime.now());
        metadata.setExpiresAt(LocalDateTime.now().plusDays(PASSWORD_EXPIRY_DAYS));
        metadata.setMustChangeOnLogin(false);
        userPasswordMetadata.put(userId, metadata);
        
        log.info("Password changed successfully for user: {}", userId);
        return true;
    }
    
    public String generateSecurePassword(int length) {
        if (length < MIN_LENGTH) length = MIN_LENGTH;
        
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()";
        String allChars = uppercase + lowercase + digits + special;
        
        StringBuilder password = new StringBuilder();
        
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = passwordArray[index];
            passwordArray[index] = passwordArray[i];
            passwordArray[i] = temp;
        }
        
        return new String(passwordArray);
    }
    
    public boolean isPasswordExpired(String userId) {
        PasswordMetadata metadata = userPasswordMetadata.get(userId);
        if (metadata == null) return false;
        
        return LocalDateTime.now().isAfter(metadata.getExpiresAt());
    }
    
    public boolean mustChangePasswordOnLogin(String userId) {
        PasswordMetadata metadata = userPasswordMetadata.get(userId);
        if (metadata == null) return false;
        
        return metadata.getMustChangeOnLogin();
    }
    
    public void recordFailedLoginAttempt(String userId) {
        LoginAttempts attempts = loginAttempts.getOrDefault(userId, new LoginAttempts());
        attempts.incrementAttempts();
        attempts.setLastAttempt(LocalDateTime.now());
        
        if (attempts.getAttempts() >= MAX_FAILED_ATTEMPTS) {
            attempts.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            log.warn("Account locked for user: {} due to failed login attempts", userId);
        }
        
        loginAttempts.put(userId, attempts);
    }
    
    public void resetFailedLoginAttempts(String userId) {
        loginAttempts.remove(userId);
    }
    
    public boolean isAccountLocked(String userId) {
        LoginAttempts attempts = loginAttempts.get(userId);
        if (attempts == null) return false;
        
        if (attempts.getLockedUntil() != null) {
            if (LocalDateTime.now().isBefore(attempts.getLockedUntil())) {
                return true;
            } else {
                loginAttempts.remove(userId);
                return false;
            }
        }
        
        return false;
    }
    
    private int calculatePasswordStrength(String password) {
        int score = 0;
        
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;
        
        if (UPPERCASE_PATTERN.matcher(password).find()) score++;
        if (LOWERCASE_PATTERN.matcher(password).find()) score++;
        if (DIGIT_PATTERN.matcher(password).find()) score++;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score++;
        
        long uniqueChars = password.chars().distinct().count();
        if (uniqueChars > password.length() * 0.7) score++;
        
        return Math.min(score, 5);
    }
    
    private PasswordStrength getPasswordStrengthLevel(int score) {
        switch (score) {
            case 1: return PasswordStrength.WEAK;
            case 2: return PasswordStrength.FAIR;
            case 3: return PasswordStrength.GOOD;
            case 4: return PasswordStrength.STRONG;
            case 5: return PasswordStrength.VERY_STRONG;
            default: return PasswordStrength.WEAK;
        }
    }
    
    private static class PasswordMetadata {
        private String userId;
        private LocalDateTime lastChanged;
        private LocalDateTime expiresAt;
        private Boolean mustChangeOnLogin;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public LocalDateTime getLastChanged() { return lastChanged; }
        public void setLastChanged(LocalDateTime lastChanged) { this.lastChanged = lastChanged; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        public Boolean getMustChangeOnLogin() { return mustChangeOnLogin; }
        public void setMustChangeOnLogin(Boolean mustChangeOnLogin) { this.mustChangeOnLogin = mustChangeOnLogin; }
    }
    
    private static class LoginAttempts {
        private Integer attempts = 0;
        private LocalDateTime lastAttempt;
        private LocalDateTime lockedUntil;
        
        public void incrementAttempts() { this.attempts++; }
        public Integer getAttempts() { return attempts; }
        public void setAttempts(Integer attempts) { this.attempts = attempts; }
        public LocalDateTime getLastAttempt() { return lastAttempt; }
        public void setLastAttempt(LocalDateTime lastAttempt) { this.lastAttempt = lastAttempt; }
        public LocalDateTime getLockedUntil() { return lockedUntil; }
        public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    }
}
