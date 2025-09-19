package com.geriatriccare.controller;

import com.geriatriccare.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    /**
     * Public endpoint - no authentication required
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint - no authentication required");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Protected endpoint - authentication required
     */
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected endpoint - authentication required");
        response.put("user", userPrincipal.getFirstName() + " " + userPrincipal.getLastName());
        response.put("email", userPrincipal.getEmail());
        response.put("authorities", userPrincipal.getAuthorities());
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Owner only endpoint - highest privilege level
     */
    @GetMapping("/owner")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Map<String, Object>> ownerOnlyEndpoint(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is an owner-only endpoint - highest privilege level");
        response.put("user", userPrincipal.getFirstName() + " " + userPrincipal.getLastName());
        response.put("role", userPrincipal.getAuthorities());
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Admin endpoint (Owner and Admin roles)
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminEndpoint(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This endpoint is for owners and admins");
        response.put("user", userPrincipal.getFirstName() + " " + userPrincipal.getLastName());
        response.put("role", userPrincipal.getAuthorities());
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Caregiver endpoint (Owner, Admin and Caregiver roles)
     */
    @GetMapping("/caregiver")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER')")
    public ResponseEntity<Map<String, Object>> caregiverEndpoint(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This endpoint is for owners, admins, and caregivers");
        response.put("user", userPrincipal.getFirstName() + " " + userPrincipal.getLastName());
        response.put("role", userPrincipal.getAuthorities());
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Family endpoint (Owner, Admin, Caregiver, and Family roles)
     */
    @GetMapping("/family")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CAREGIVER') or hasRole('FAMILY')")
    public ResponseEntity<Map<String, Object>> familyEndpoint(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This endpoint is for owners, admins, family members, and caregivers");
        response.put("user", userPrincipal.getFirstName() + " " + userPrincipal.getLastName());
        response.put("role", userPrincipal.getAuthorities());
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}