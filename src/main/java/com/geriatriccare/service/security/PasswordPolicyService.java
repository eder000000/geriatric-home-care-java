package com.geriatriccare.service.security;

import com.geriatriccare.dto.security.PasswordValidationRequest;
import com.geriatriccare.dto.security.PasswordValidationResponse;
import com.geriatriccare.entity.PasswordHistory;
import com.geriatriccare.entity.User;
import com.geriatriccare.exception.InvalidPasswordException;
import com.geriatriccare.repository.PasswordHistoryRepository;
import com.geriatriccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PasswordPolicyService {
    
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int PASSWORD_HISTORY_SIZE = 5;
    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;
    
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_DIGIT = "0123456789";
    private static final String CHAR_SPECIAL = "!@#$%^&*(),.?\":{}|<>";
    private static final SecureRandom random = new SecureRandom();

    @Autowired private PasswordHistoryRepository passwordHistoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // New simplified method for basic validation
    public void validatePassword(String password, String username) {
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidPasswordException("Password cannot be empty");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (!UPPERCASE.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain uppercase letter");
        }
        if (!LOWERCASE.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain lowercase letter");
        }
        if (!DIGIT.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain digit");
        }
        if (!SPECIAL.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain special character");
        }
        if (username != null && password.equalsIgnoreCase(username)) {
            throw new InvalidPasswordException("Password cannot be same as username");
        }
    }

    // Original method for PasswordPolicyController
    public PasswordValidationResponse validatePassword(PasswordValidationRequest request) {
        PasswordValidationResponse response = new PasswordValidationResponse();
        response.setIsValid(true);
        try {
            validatePassword(request.getPassword(), request.getUsername());
            response.setMessage("Password meets all requirements");
        } catch (InvalidPasswordException e) {
            response.setIsValid(false);
            response.setMessage(e.getMessage());
        }
        
        return response;
    }

    public boolean isPasswordInHistory(UUID userId, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository.findByUserIdOrderByChangedAtDesc(userId);
        int checkCount = Math.min(PASSWORD_HISTORY_SIZE, history.size());
        for (int i = 0; i < checkCount; i++) {
            if (passwordEncoder.matches(newPassword, history.get(i).getPasswordHash())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void addToPasswordHistory(UUID userId, String passwordHash) {
        PasswordHistory history = new PasswordHistory();
        history.setUserId(userId);
        history.setPasswordHash(passwordHash);
        history.setChangedAt(LocalDateTime.now());
        passwordHistoryRepository.save(history);

        List<PasswordHistory> allHistory = passwordHistoryRepository.findByUserIdOrderByChangedAtDesc(userId);
        if (allHistory.size() > PASSWORD_HISTORY_SIZE) {
            passwordHistoryRepository.deleteAll(allHistory.subList(PASSWORD_HISTORY_SIZE, allHistory.size()));
        }
    }

    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        UUID id = UUID.fromString(userId);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            incrementFailedLoginAttempts(user);
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        validatePassword(newPassword, user.getUsername());
        
        if (isPasswordInHistory(id, newPassword)) {
            throw new InvalidPasswordException("Cannot reuse last 5 passwords");
        }
        
        String encoded = passwordEncoder.encode(newPassword);
        user.setPassword(encoded);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        
        addToPasswordHistory(id, encoded);
    }

    public String generateSecurePassword(int length) {
        if (length < MIN_PASSWORD_LENGTH) {
            length = MIN_PASSWORD_LENGTH;
        }
        
        StringBuilder password = new StringBuilder(length);
        String allChars = CHAR_UPPER + CHAR_LOWER + CHAR_DIGIT + CHAR_SPECIAL;
        
        // Ensure at least one of each required character type
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_DIGIT.charAt(random.nextInt(CHAR_DIGIT.length())));
        password.append(CHAR_SPECIAL.charAt(random.nextInt(CHAR_SPECIAL.length())));
        
        // Fill remaining length with random characters
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString());
    }

    public boolean isPasswordExpired(String userId) {
        UUID id = UUID.fromString(userId);
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return true;
        }
        
        User user = userOpt.get();
        if (user.getPasswordChangedAt() == null) {
            return true;
        }
        
        LocalDateTime expiryDate = user.getPasswordChangedAt().plusDays(PASSWORD_EXPIRY_DAYS);
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean mustChangePasswordOnLogin(String userId) {
        UUID id = UUID.fromString(userId);
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(User::getMustChangePassword).orElse(false);
    }

    public boolean isAccountLocked(String userId) {
        UUID id = UUID.fromString(userId);
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        if (user.getLockedUntil() == null) {
            return false;
        }
        
        return LocalDateTime.now().isBefore(user.getLockedUntil());
    }

    @Transactional
    public void resetFailedLoginAttempts(String userId) {
        UUID id = UUID.fromString(userId);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    @Transactional
    private void incrementFailedLoginAttempts(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(ACCOUNT_LOCK_DURATION_MINUTES));
        }
        
        userRepository.save(user);
    }

    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}