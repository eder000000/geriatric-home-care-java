package com.geriatriccare.service.security;

import com.geriatriccare.dto.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SessionManagementService {
    
    private static final Logger log = LoggerFactory.getLogger(SessionManagementService.class);
    
    private final SessionConfiguration config = new SessionConfiguration();
    private final Map<UUID, UserSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, List<UUID>> userSessions = new ConcurrentHashMap<>();
    
    private int expiredToday = 0;
    private int revokedToday = 0;
    private int peakConcurrent = 0;
    
    public UserSession createSession(String userId, String username, String ipAddress, 
                                     String userAgent, Boolean rememberMe) {
        log.info("Creating session for user: {}", userId);
        
        // Check concurrent session limit
        List<UUID> existingSessions = userSessions.getOrDefault(userId, new ArrayList<>());
        List<UUID> activeSessions = existingSessions.stream()
                .filter(sessionId -> {
                    UserSession session = sessions.get(sessionId);
                    return session != null && session.getStatus() == SessionStatus.ACTIVE;
                })
                .collect(Collectors.toList());
        
        // Remove oldest session if at limit
        if (activeSessions.size() >= config.getMaxConcurrentSessions()) {
            UUID oldestSessionId = activeSessions.get(0);
            log.info("Max concurrent sessions reached for user: {}. Revoking oldest session.", userId);
            revokeSession(oldestSessionId);
        }
        
        // Create new session
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setUsername(username);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceType(detectDeviceType(userAgent));
        session.setRememberMe(rememberMe != null ? rememberMe : false);
        
        // Set expiration
        int timeoutMinutes = session.getRememberMe() ? 
                (config.getRememberMeDays() * 24 * 60) : config.getTimeoutMinutes();
        session.setExpiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes));
        
        // Store session
        sessions.put(session.getSessionId(), session);
        
        // Update user sessions list
        existingSessions.add(session.getSessionId());
        userSessions.put(userId, existingSessions);
        
        // Update peak
        int currentActive = getActiveSessionCount();
        if (currentActive > peakConcurrent) {
            peakConcurrent = currentActive;
        }
        
        log.info("Session created: {} for user: {}", session.getSessionId(), userId);
        return session;
    }
    
    public boolean renewSession(UUID sessionId) {
        UserSession session = sessions.get(sessionId);
        
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            return false;
        }
        
        if (session.getStatus() != SessionStatus.ACTIVE) {
            log.warn("Cannot renew non-active session: {}", sessionId);
            return false;
        }
        
        // Update activity and expiration
        session.setLastActivityAt(LocalDateTime.now());
        
        int timeoutMinutes = session.getRememberMe() ? 
                (config.getRememberMeDays() * 24 * 60) : config.getTimeoutMinutes();
        session.setExpiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes));
        
        sessions.put(sessionId, session);
        
        log.debug("Session renewed: {}", sessionId);
        return true;
    }
    
    public boolean revokeSession(UUID sessionId) {
        UserSession session = sessions.get(sessionId);
        
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            return false;
        }
        
        session.setStatus(SessionStatus.REVOKED);
        sessions.put(sessionId, session);
        
        revokedToday++;
        
        log.info("Session revoked: {}", sessionId);
        return true;
    }
    
    public int revokeAllUserSessions(String userId) {
        log.info("Revoking all sessions for user: {}", userId);
        
        List<UUID> userSessionIds = userSessions.getOrDefault(userId, new ArrayList<>());
        int revokedCount = 0;
        
        for (UUID sessionId : userSessionIds) {
            if (revokeSession(sessionId)) {
                revokedCount++;
            }
        }
        
        log.info("Revoked {} sessions for user: {}", revokedCount, userId);
        return revokedCount;
    }
    
    public List<UserSession> getUserSessions(String userId) {
        List<UUID> sessionIds = userSessions.getOrDefault(userId, new ArrayList<>());
        
        return sessionIds.stream()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    public List<UserSession> getActiveSessions(String userId) {
        return getUserSessions(userId).stream()
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> !isSessionExpired(session))
                .collect(Collectors.toList());
    }
    
    public List<UserSession> getAllActiveSessions() {
        return sessions.values().stream()
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> !isSessionExpired(session))
                .collect(Collectors.toList());
    }
    
    public SessionStatistics getStatistics() {
        SessionStatistics stats = new SessionStatistics();
        
        stats.setTotalActiveSessions(getActiveSessionCount());
        stats.setTotalUsers((int) userSessions.keySet().stream()
                .filter(userId -> !getActiveSessions(userId).isEmpty())
                .count());
        stats.setExpiredSessionsToday(expiredToday);
        stats.setRevokedSessionsToday(revokedToday);
        stats.setPeakConcurrentSessions(peakConcurrent);
        
        // Calculate average session duration
        List<UserSession> allSessions = new ArrayList<>(sessions.values());
        if (!allSessions.isEmpty()) {
            double avgMinutes = allSessions.stream()
                    .mapToLong(s -> ChronoUnit.MINUTES.between(s.getCreatedAt(), s.getLastActivityAt()))
                    .average()
                    .orElse(0.0);
            stats.setAverageSessionDurationMinutes(avgMinutes);
        }
        
        return stats;
    }
    
    public SessionConfiguration getConfiguration() {
        return config;
    }
    
    public void updateConfiguration(SessionConfiguration newConfig) {
        if (newConfig.getTimeoutMinutes() != null) {
            config.setTimeoutMinutes(newConfig.getTimeoutMinutes());
        }
        if (newConfig.getMaxConcurrentSessions() != null) {
            config.setMaxConcurrentSessions(newConfig.getMaxConcurrentSessions());
        }
        if (newConfig.getRememberMeDays() != null) {
            config.setRememberMeDays(newConfig.getRememberMeDays());
        }
        
        log.info("Session configuration updated");
    }
    
    @Scheduled(fixedDelay = 900000) // 15 minutes
    public void cleanupExpiredSessions() {
        if (!config.getAutoCleanup()) {
            return;
        }
        
        log.info("Running expired session cleanup...");
        
        int cleanedCount = 0;
        List<UUID> toRemove = new ArrayList<>();
        
        for (Map.Entry<UUID, UserSession> entry : sessions.entrySet()) {
            UserSession session = entry.getValue();
            
            if (isSessionExpired(session) && session.getStatus() == SessionStatus.ACTIVE) {
                session.setStatus(SessionStatus.EXPIRED);
                sessions.put(entry.getKey(), session);
                expiredToday++;
                cleanedCount++;
            }
            
            // Remove old expired/revoked sessions (older than 7 days)
            if (session.getStatus() != SessionStatus.ACTIVE) {
                LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
                if (session.getLastActivityAt().isBefore(cutoff)) {
                    toRemove.add(entry.getKey());
                }
            }
        }
        
        // Remove old sessions
        for (UUID sessionId : toRemove) {
            sessions.remove(sessionId);
        }
        
        log.info("Cleanup complete: {} sessions expired, {} old sessions removed", 
                 cleanedCount, toRemove.size());
    }
    
    private boolean isSessionExpired(UserSession session) {
        return LocalDateTime.now().isAfter(session.getExpiresAt());
    }
    
    private int getActiveSessionCount() {
        return (int) sessions.values().stream()
                .filter(session -> session.getStatus() == SessionStatus.ACTIVE)
                .filter(session -> !isSessionExpired(session))
                .count();
    }
    
    private String detectDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "unknown";
    }
}
