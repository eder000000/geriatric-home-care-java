package com.geriatriccare.service.security;

import com.geriatriccare.exception.InvalidPasswordException;
import com.geriatriccare.model.security.PasswordHistory;
import com.geriatriccare.repository.security.PasswordHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PasswordPolicyService {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int PASSWORD_HISTORY_SIZE = 5;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    @Autowired private PasswordHistoryRepository passwordHistoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;

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
}
