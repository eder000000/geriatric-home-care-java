package com.geriatriccare.controller.security;

import com.geriatriccare.dto.security.*;
import com.geriatriccare.service.security.PasswordPolicyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/security/password")
public class PasswordPolicyController {
    
    private static final Logger log = LoggerFactory.getLogger(PasswordPolicyController.class);
    
    private final PasswordPolicyService passwordPolicyService;
    
    @Autowired
    public PasswordPolicyController(PasswordPolicyService passwordPolicyService) {
        this.passwordPolicyService = passwordPolicyService;
    }
    
    @PostMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PasswordValidationResponse> validatePassword(
            @Valid @RequestBody PasswordValidationRequest request) {
        
        log.info("Password validation request received");
        
        try {
            PasswordValidationResponse response = passwordPolicyService.validatePassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Password validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        
        log.info("Password change request received");
        
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }
        
        try {
            passwordPolicyService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        
        log.info("Password reset request received");
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }
        
        try {
            String userId = "user-from-token";
            
            passwordPolicyService.changePassword(userId, null, request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (RuntimeException e) {
            log.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> generatePassword(@RequestParam(defaultValue = "16") int length) {
        
        log.info("Generate password request - length: {}", length);
        
        try {
            String password = passwordPolicyService.generateSecurePassword(length);
            return ResponseEntity.ok(password);
        } catch (Exception e) {
            log.error("Password generation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getPasswordStatus() {
        
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        log.info("Password status request for user: {}", userId);
        
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("expired", passwordPolicyService.isPasswordExpired(userId));
        status.put("mustChange", passwordPolicyService.mustChangePasswordOnLogin(userId));
        status.put("accountLocked", passwordPolicyService.isAccountLocked(userId));
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/unlock/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unlockAccount(@PathVariable String userId) {
        
        log.info("Unlock account request for user: {}", userId);
        
        try {
            passwordPolicyService.resetFailedLoginAttempts(userId);
            return ResponseEntity.ok("Account unlocked successfully");
        } catch (Exception e) {
            log.error("Account unlock failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Password Policy Service is running");
    }
}
