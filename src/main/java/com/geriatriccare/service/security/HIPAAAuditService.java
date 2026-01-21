package com.geriatriccare.service.security;

import com.geriatriccare.dto.security.*;
import com.geriatriccare.enums.AuditEventType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;




@Service
public class HIPAAAuditService {
    
    private static final Logger log = LoggerFactory.getLogger(HIPAAAuditService.class);
    private static final int RETENTION_DAYS = 2555; // 7 years HIPAA requirement
    
    private final Map<UUID, AuditEvent> auditEvents = new ConcurrentHashMap<>();
    private int dailyPHIAccessCount = 0;
    private int dailySecurityEventCount = 0;
    
    public AuditEvent logEvent(AuditEvent event) {
        log.info("Logging audit event: {} for user: {}", event.getEventType(), event.getUserId());
        
        // Set automatic fields
        if (event.getUserId() == null) {
            event.setUserId(getCurrentUserId());
            event.setUsername(getCurrentUsername());
        }
        
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        
        // Set severity based on event type
        if (event.getSeverity() == null) {
            event.setSeverity(determineEventSeverity(event.getEventType()));
        }
        
        // Generate tamper-proof checksum
        event.setChecksumHash(generateChecksum(event));
        
        // Store event
        auditEvents.put(event.getEventId(), event);
        
        // Update counters
        if (event.getEventType().isPHIEvent()) {
            dailyPHIAccessCount++;
        }
        if (event.getEventType().isSecurityCritical()) {
            dailySecurityEventCount++;
            
            // Log security-critical events at WARN level
            log.warn("SECURITY EVENT: {} by user {} from IP {}", 
                     event.getEventType(), event.getUserId(), event.getIpAddress());
        }
        
        return event;
    }
    
    public AuditEvent logPHIAccess(UUID patientId, String patientName, String action, String resourceType) {
        AuditEvent event = new AuditEvent();
        event.setEventType(AuditEventType.PHI_VIEW);
        event.setPatientId(patientId);
        event.setPatientName(patientName);
        event.setAction(action);
        event.setResourceType(resourceType);
        event.setSeverity(AuditEventSeverity.HIGH);
        
        return logEvent(event);
    }
    
    public AuditEvent logSecurityEvent(AuditEventType eventType, String description, String ipAddress) {
        AuditEvent event = new AuditEvent();
        event.setEventType(eventType);
        event.setAction(description);
        event.setIpAddress(ipAddress);
        event.setSeverity(AuditEventSeverity.HIGH);
        
        return logEvent(event);
    }
    
    public List<AuditEvent> queryAuditEvents(AuditFilter filter) {
        log.info("Querying audit events with filter");
        
        return auditEvents.values().stream()
                .filter(event -> matchesFilter(event, filter))
                .sorted(Comparator.comparing(AuditEvent::getTimestamp).reversed())
                .skip(filter.getOffset())
                .limit(filter.getLimit())
                .collect(Collectors.toList());
    }
    
    public AuditReport generateReport(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        log.info("Generating audit report: {} from {} to {}", reportType, startDate, endDate);
        
        AuditReport report = new AuditReport();
        report.setReportType(reportType);
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);
        
        // Filter events in date range
        List<AuditEvent> filteredEvents = auditEvents.values().stream()
                .filter(e -> e.getTimestamp().isAfter(startDate) && e.getTimestamp().isBefore(endDate))
                .sorted(Comparator.comparing(AuditEvent::getTimestamp))
                .collect(Collectors.toList());
        
        report.setEvents(filteredEvents);
        report.setTotalEvents(filteredEvents.size());
        
        // Calculate statistics
        report.setPhiAccessCount((int) filteredEvents.stream()
                .filter(e -> e.getEventType().isPHIEvent())
                .count());
        
        report.setSecurityEventsCount((int) filteredEvents.stream()
                .filter(e -> e.getEventType().isSecurityCritical())
                .count());
        
        report.setFailedLoginsCount((int) filteredEvents.stream()
                .filter(e -> e.getEventType() == AuditEventType.LOGIN_FAILURE)
                .count());
        
        report.setUnauthorizedAccessCount((int) filteredEvents.stream()
                .filter(e -> e.getEventType() == AuditEventType.UNAUTHORIZED_ACCESS)
                .count());
        
        // Event type breakdown
        Map<String, Integer> typeBreakdown = new HashMap<>();
        for (AuditEvent event : filteredEvents) {
            String type = event.getEventType().getDisplayName();
            typeBreakdown.put(type, typeBreakdown.getOrDefault(type, 0) + 1);
        }
        report.setEventTypeBreakdown(typeBreakdown);
        
        // User activity breakdown
        Map<String, Integer> userBreakdown = new HashMap<>();
        for (AuditEvent event : filteredEvents) {
            String user = event.getUsername() != null ? event.getUsername() : event.getUserId();
            userBreakdown.put(user, userBreakdown.getOrDefault(user, 0) + 1);
        }
        report.setUserActivityBreakdown(userBreakdown);
        
        // Severity breakdown
        Map<String, Integer> severityBreakdown = new HashMap<>();
        for (AuditEvent event : filteredEvents) {
            String severity = event.getSeverity().getDisplayName();
            severityBreakdown.put(severity, severityBreakdown.getOrDefault(severity, 0) + 1);
        }
        report.setSeverityBreakdown(severityBreakdown);
        
        // Detect compliance violations
        detectComplianceViolations(filteredEvents, report);
        
        // Generate summary
        report.setSummary(String.format(
            "Audit report for period %s to %s: %d total events, %d PHI accesses, %d security events, %d failed logins",
            startDate, endDate, report.getTotalEvents(), report.getPhiAccessCount(), 
            report.getSecurityEventsCount(), report.getFailedLoginsCount()
        ));
        
        log.info("Report generated: {} events analyzed", filteredEvents.size());
        return report;
    }
    
    public ComplianceStatus checkComplianceStatus() {
        log.info("Checking HIPAA compliance status");
        
        ComplianceStatus status = new ComplianceStatus();
        status.setAuditLogRetentionDays(RETENTION_DAYS);
        status.setTamperProofingEnabled(true);
        status.setPhiAccessLogging(true);
        status.setSecurityEventMonitoring(true);
        status.setLastAuditDate(LocalDateTime.now());
        status.setNextAuditDate(LocalDateTime.now().plusDays(30));
        
        // Check for compliance issues
        List<String> issues = new ArrayList<>();
        
        // Check PHI access logging
        long phiEvents = auditEvents.values().stream()
                .filter(e -> e.getEventType().isPHIEvent())
                .count();
        
        if (phiEvents == 0) {
            issues.add("No PHI access events logged - verify logging is active");
        }
        
        // Check for security event monitoring
        long securityEvents = auditEvents.values().stream()
                .filter(e -> e.getEventType().isSecurityCritical())
                .filter(e -> e.getTimestamp().isAfter(LocalDateTime.now().minusDays(7)))
                .count();
        
        if (securityEvents == 0) {
            issues.add("No recent security events logged - verify monitoring is active");
        }
        
        // Check for tamper detection
        int tamperedEvents = verifyLogIntegrity();
        if (tamperedEvents > 0) {
            issues.add("CRITICAL: " + tamperedEvents + " tampered audit log entries detected");
        }
        
        status.setComplianceIssues(issues);
        status.setHipaaCompliant(issues.isEmpty() || !issues.stream().anyMatch(i -> i.contains("CRITICAL")));
        
        // Recommendations
        List<String> recommendations = new ArrayList<>();
        if (!status.getHipaaCompliant()) {
            recommendations.add("Address critical compliance issues immediately");
        }
        recommendations.add("Conduct monthly compliance audits");
        recommendations.add("Review and update security policies quarterly");
        recommendations.add("Ensure all staff complete HIPAA training annually");
        
        status.setRecommendations(recommendations);
        
        return status;
    }
    
    public boolean verifyEventIntegrity(UUID eventId) {
        AuditEvent event = auditEvents.get(eventId);
        if (event == null) {
            return false;
        }
        
        String storedHash = event.getChecksumHash();
        String calculatedHash = generateChecksum(event);
        
        return storedHash != null && storedHash.equals(calculatedHash);
    }
    
    public int verifyLogIntegrity() {
        log.info("Verifying audit log integrity");
        
        int tamperedCount = 0;
        for (AuditEvent event : auditEvents.values()) {
            if (!verifyEventIntegrity(event.getEventId())) {
                log.error("TAMPERED LOG DETECTED: Event {} has been modified", event.getEventId());
                tamperedCount++;
            }
        }
        
        if (tamperedCount > 0) {
            log.error("SECURITY ALERT: {} tampered audit log entries detected", tamperedCount);
        }
        
        return tamperedCount;
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void cleanupOldLogs() {
        log.info("Running audit log retention cleanup");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
        List<UUID> toRemove = new ArrayList<>();
        
        for (Map.Entry<UUID, AuditEvent> entry : auditEvents.entrySet()) {
            if (entry.getValue().getTimestamp().isBefore(cutoffDate)) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (UUID eventId : toRemove) {
            auditEvents.remove(eventId);
        }
        
        log.info("Cleanup complete: {} old audit logs removed (retention: {} days)", 
                 toRemove.size(), RETENTION_DAYS);
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void resetDailyCounters() {
        log.info("Resetting daily audit counters - PHI: {}, Security: {}", 
                 dailyPHIAccessCount, dailySecurityEventCount);
        dailyPHIAccessCount = 0;
        dailySecurityEventCount = 0;
    }
    
    private boolean matchesFilter(AuditEvent event, AuditFilter filter) {
        if (filter.getEventTypes() != null && !filter.getEventTypes().isEmpty()) {
            if (!filter.getEventTypes().contains(event.getEventType())) {
                return false;
            }
        }
        
        if (filter.getSeverities() != null && !filter.getSeverities().isEmpty()) {
            if (!filter.getSeverities().contains(event.getSeverity())) {
                return false;
            }
        }
        
        if (filter.getUserId() != null && !filter.getUserId().equals(event.getUserId())) {
            return false;
        }
        
        if (filter.getPatientId() != null && !filter.getPatientId().equals(event.getPatientId())) {
            return false;
        }
        
        if (filter.getStartDate() != null && event.getTimestamp().isBefore(filter.getStartDate())) {
            return false;
        }
        
        if (filter.getEndDate() != null && event.getTimestamp().isAfter(filter.getEndDate())) {
            return false;
        }
        
        if (filter.getPhiEventsOnly() && !event.getEventType().isPHIEvent()) {
            return false;
        }
        
        if (filter.getSecurityEventsOnly() && !event.getEventType().isSecurityCritical()) {
            return false;
        }
        
        return true;
    }
    
    private AuditEventSeverity determineEventSeverity(AuditEventType eventType) {
        if (eventType.isPHIEvent()) {
            return AuditEventSeverity.HIGH;
        }
        
        switch (eventType) {
            case LOGIN_FAILURE:
            case UNAUTHORIZED_ACCESS:
            case SECURITY_VIOLATION:
                return AuditEventSeverity.HIGH;
            case SESSION_EXPIRED:
            case MFA_DISABLED:
                return AuditEventSeverity.MEDIUM;
            case LOGIN_SUCCESS:
            case LOGOUT:
                return AuditEventSeverity.INFO;
            default:
                return AuditEventSeverity.LOW;
        }
    }
    
    private void detectComplianceViolations(List<AuditEvent> events, AuditReport report) {
        List<String> violations = new ArrayList<>();
        
        // Check for excessive failed logins
        Map<String, Long> failedLoginsByUser = events.stream()
                .filter(e -> e.getEventType() == AuditEventType.LOGIN_FAILURE)
                .collect(Collectors.groupingBy(AuditEvent::getUserId, Collectors.counting()));
        
        for (Map.Entry<String, Long> entry : failedLoginsByUser.entrySet()) {
            if (entry.getValue() > 5) {
                violations.add("User " + entry.getKey() + " has " + entry.getValue() + " failed login attempts");
            }
        }
        
        // Check for unauthorized access attempts
        long unauthorizedAttempts = events.stream()
                .filter(e -> e.getEventType() == AuditEventType.UNAUTHORIZED_ACCESS)
                .count();
        
        if (unauthorizedAttempts > 0) {
            violations.add(unauthorizedAttempts + " unauthorized access attempts detected");
        }
        
        report.setComplianceViolations(violations);
    }
    
    private String generateChecksum(AuditEvent event) {
        try {
            String data = String.format("%s|%s|%s|%s|%s|%s",
                    event.getEventId(),
                    event.getEventType(),
                    event.getUserId(),
                    event.getTimestamp(),
                    event.getAction(),
                    event.getResourceId()
            );
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to generate checksum: {}", e.getMessage());
            return null;
        }
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "system";
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getName();
        }
        return "system";
    }
}
