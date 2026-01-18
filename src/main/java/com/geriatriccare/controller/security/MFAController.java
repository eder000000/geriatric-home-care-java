package com.geriatriccare.controller.security;

import com.geriatriccare.dto.security.*;
import com.geriatriccare.service.security.MFAService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/mfa")
public class MFAController {
    
    private static final Logger log = LoggerFactory.getLogger(MFAController.class);
    
    private final MFAService mfaService;
    
    @Autowired
    public MFAController(MFAService mfaService) {
        this.mfaService = mfaService;
    }
    
    @PostMapping("/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MFASetupResponse> setupMFA(@Valid @RequestBody MFASetupRequest request) {
        log.info("MFA setup request received");
        
        try {
            MFASetupResponse response = mfaService.setupMFA(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("MFA setup failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/send-code")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MFAVerificationResponse> sendCode() {
        log.info("MFA code request received");
        
        MFAVerificationResponse response = mfaService.sendVerificationCode();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MFAVerificationResponse> verifyCode(
            @Valid @RequestBody MFAVerificationRequest request) {
        log.info("MFA verification request received");
        
        MFAVerificationResponse response = mfaService.verifyCode(request);
        
        if (response.getVerified()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }
    
    @PostMapping("/enable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> enableMFA() {
        log.info("Enable MFA request received");
        
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        
        mfaService.enableMFA(userId);
        return ResponseEntity.ok("MFA enabled successfully");
    }
    
    @PostMapping("/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> disableMFA(@RequestParam String userId) {
        log.info("Disable MFA request for user: {}", userId);
        
        mfaService.disableMFA(userId);
        return ResponseEntity.ok("MFA disabled for user: " + userId);
    }
    
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MFAStatus> getStatus() {
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        
        MFAStatus status = mfaService.getMFAStatus(userId);
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MFA Service is running");
    }
}
