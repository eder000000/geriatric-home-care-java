package com.geriatriccare.controller.security;

import com.geriatriccare.dto.security.*;
import com.geriatriccare.service.security.SessionManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/security/sessions")
public class SessionManagementController {
    
    private static final Logger log = LoggerFactory.getLogger(SessionManagementController.class);
    
    private final SessionManagementService sessionService;
    
    @Autowired
    public SessionManagementController(SessionManagementService sessionService) {
        this.sessionService = sessionService;
    }
    
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserSession>> getActiveSessions() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        log.info("Get active sessions request for user: {}", userId);
        
        List<UserSession> sessions = sessionService.getActiveSessions(userId);
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSession>> getAllActiveSessions() {
        
        log.info("Get all active sessions request (Admin)");
        
        List<UserSession> sessions = sessionService.getAllActiveSessions();
        return ResponseEntity.ok(sessions);
    }
    
    @PostMapping("/renew")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> renewSession(@RequestParam UUID sessionId) {
        
        log.info("Renew session request: {}", sessionId);
        
        boolean renewed = sessionService.renewSession(sessionId);
        
        if (renewed) {
            return ResponseEntity.ok("Session renewed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to renew session");
        }
    }
    
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> revokeSession(@PathVariable UUID sessionId) {
        
        log.info("Revoke session request: {}", sessionId);
        
        boolean revoked = sessionService.revokeSession(sessionId);
        
        if (revoked) {
            return ResponseEntity.ok("Session revoked successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to revoke session");
        }
    }
    
    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logoutAllDevices() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        log.info("Logout all devices request for user: {}", userId);
        
        int revokedCount = sessionService.revokeAllUserSessions(userId);
        
        return ResponseEntity.ok("Logged out from " + revokedCount + " devices");
    }
    
    @PostMapping("/force-logout/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> forceLogout(@PathVariable String userId) {
        
        log.info("Force logout request for user: {} (Admin action)", userId);
        
        int revokedCount = sessionService.revokeAllUserSessions(userId);
        
        return ResponseEntity.ok("Force logged out user " + userId + " from " + revokedCount + " sessions");
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionStatistics> getStatistics() {
        
        log.info("Get session statistics request (Admin)");
        
        SessionStatistics stats = sessionService.getStatistics();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionConfiguration> getConfiguration() {
        
        log.info("Get session configuration request (Admin)");
        
        SessionConfiguration config = sessionService.getConfiguration();
        return ResponseEntity.ok(config);
    }
    
    @PutMapping("/configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateConfiguration(@RequestBody SessionConfiguration config) {
        
        log.info("Update session configuration request (Admin)");
        
        sessionService.updateConfiguration(config);
        return ResponseEntity.ok("Configuration updated successfully");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Session Management Service is running");
    }
}
