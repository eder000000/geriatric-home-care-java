package com.geriatriccare.service.security;

import com.geriatriccare.dto.security.RateLimitConfig;
import com.geriatriccare.dto.security.RateLimitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    
    private final RateLimitConfig config = new RateLimitConfig();
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final Set<String> whitelist = new HashSet<>(Arrays.asList(
        "127.0.0.1", "localhost"
    ));
    
    public boolean allowRequest(String identifier) {
        if (!config.getEnabled()) {
            return true;
        }
        
        if (whitelist.contains(identifier)) {
            logger.debug("Request allowed - whitelisted: {}", identifier);
            return true;
        }
        
        TokenBucket bucket = buckets.computeIfAbsent(identifier, k -> new TokenBucket(config.getRequestsPerMinute()));
        
        boolean allowed = bucket.tryConsume();
        
        if (!allowed) {
            logger.warn("Rate limit exceeded for: {}", identifier);
        }
        
        return allowed;
    }
    
    public RateLimitInfo getRateLimitInfo(String identifier) {
        RateLimitInfo info = new RateLimitInfo();
        info.setIdentifier(identifier);
        info.setLimit(config.getRequestsPerMinute());
        
        TokenBucket bucket = buckets.get(identifier);
        if (bucket != null) {
            info.setRemaining(bucket.getAvailableTokens());
            info.setResetTime(bucket.getResetTime());
            info.setIsBlocked(bucket.getAvailableTokens() <= 0);
        } else {
            info.setRemaining(config.getRequestsPerMinute());
            info.setResetTime(LocalDateTime.now().plusMinutes(1));
            info.setIsBlocked(false);
        }
        
        return info;
    }
    
    public void addToWhitelist(String identifier) {
        whitelist.add(identifier);
        logger.info("Added to rate limit whitelist: {}", identifier);
    }
    
    public void removeFromWhitelist(String identifier) {
        whitelist.remove(identifier);
        logger.info("Removed from rate limit whitelist: {}", identifier);
    }
    
    public RateLimitConfig getConfig() {
        return config;
    }
    
    public void updateConfig(RateLimitConfig newConfig) {
        if (newConfig.getRequestsPerMinute() != null) {
            config.setRequestsPerMinute(newConfig.getRequestsPerMinute());
        }
        if (newConfig.getRequestsPerHour() != null) {
            config.setRequestsPerHour(newConfig.getRequestsPerHour());
        }
        if (newConfig.getRequestsPerDay() != null) {
            config.setRequestsPerDay(newConfig.getRequestsPerDay());
        }
        if (newConfig.getEnabled() != null) {
            config.setEnabled(newConfig.getEnabled());
        }
        
        logger.info("Rate limit configuration updated");
    }
    
    // Token Bucket Algorithm Implementation
    private static class TokenBucket {
        private final int capacity;
        private int tokens;
        private LocalDateTime lastRefill;
        
        public TokenBucket(int capacity) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.lastRefill = LocalDateTime.now();
        }
        
        public synchronized boolean tryConsume() {
            refill();
            
            if (tokens > 0) {
                tokens--;
                return true;
            }
            
            return false;
        }
        
        private void refill() {
            LocalDateTime now = LocalDateTime.now();
            long minutesPassed = ChronoUnit.MINUTES.between(lastRefill, now);
            
            if (minutesPassed > 0) {
                tokens = capacity;
                lastRefill = now;
            }
        }
        
        public int getAvailableTokens() {
            refill();
            return tokens;
        }
        
        public LocalDateTime getResetTime() {
            return lastRefill.plusMinutes(1);
        }
    }
}
