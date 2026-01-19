package com.geriatriccare.controller.security;

import com.geriatriccare.dto.security.RateLimitConfig;
import com.geriatriccare.dto.security.RateLimitInfo;
import com.geriatriccare.service.security.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitController {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitController.class);
    
    private final RateLimitService rateLimitService;
    
    @Autowired
    public RateLimitController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }
    
    @GetMapping("/info")
    public ResponseEntity<RateLimitInfo> getRateLimitInfo(HttpServletRequest request) {
        String ipAddress = getClientIP(request);
        
        logger.info("Rate limit info request from: {}", ipAddress);
        
        RateLimitInfo info = rateLimitService.getRateLimitInfo(ipAddress);
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateLimitConfig> getConfig() {
        
        logger.info("Rate limit config request");
        
        RateLimitConfig config = rateLimitService.getConfig();
        return ResponseEntity.ok(config);
    }
    
    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateConfig(@RequestBody RateLimitConfig config) {
        
        logger.info("Rate limit config update request");
        
        rateLimitService.updateConfig(config);
        return ResponseEntity.ok("Rate limit configuration updated successfully");
    }
    
    @PostMapping("/whitelist/{identifier}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addToWhitelist(@PathVariable String identifier) {
        
        logger.info("Add to whitelist request: {}", identifier);
        
        rateLimitService.addToWhitelist(identifier);
        return ResponseEntity.ok("Added to whitelist: " + identifier);
    }
    
    @DeleteMapping("/whitelist/{identifier}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> removeFromWhitelist(@PathVariable String identifier) {
        
        logger.info("Remove from whitelist request: {}", identifier);
        
        rateLimitService.removeFromWhitelist(identifier);
        return ResponseEntity.ok("Removed from whitelist: " + identifier);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Rate Limit Service is running");
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        return request.getRemoteAddr();
    }
}
