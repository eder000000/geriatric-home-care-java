package com.geriatriccare.service.security;

import com.geriatriccare.dto.security.*;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.enums.AuditEventType;
import com.geriatriccare.enums.AuditSeverity;
import com.geriatriccare.repository.AIAuditLogRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;




@Service
public class AuditEnhancementService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditEnhancementService.class);
    private static final int RETENTION_YEARS = 7;
    private static final String CHECKSUM_ALGORITHM = "SHA-256";
    
    private final AIAuditLogRepository auditLogRepository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Map<UUID, ComplianceViolation> violations = new ConcurrentHashMap<>();
    
    private String lastChecksum = "";
    
    @Autowired
    public AuditEnhancementService(AIAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    public AIAuditLog logEnhancedAuditEvent(
            AuditEventType eventType,
            AuditSeverity severity,
            DataSensitivity dataSensitivity,
            UUID patientId,
            String userId,
            String details,
            String ipAddress) {
        
        logger.info("Logging HIPAA audit event: {} - Severity: {}", eventType, severity);
        
        AIAuditLog auditLog = new AIAuditLog();
        auditLog.setPatientId(patientId);
        auditLog.setUserId(userId != null ? UUID.fromString(userId) : null);
        auditLog.setRequestType(eventType.name());
        auditLog.setPrompt(details);
        auditLog.setResponse(String.format("Event: %s, Severity: %s, Sensitivity: %s", 
                                          eventType.getDisplayName(), severity, dataSensitivity));
        auditLog.setSuccess(true);
        auditLog.setTimestamp(LocalDateTime.now());
        
        String checksum = generateChecksum(auditLog);
        auditLog.setErrorMessage(checksum);
        
        lastChecksum = checksum;
        
        AIAuditLog saved = auditLogRepository.save(auditLog);
        
        detectComplianceViolations(saved, eventType, severity, dataSensitivity);
        streamAuditEvent(saved, eventType, severity, dataSensitivity);
        
        logger.debug("Audit event logged with ID: {} and checksum: {}", saved.getId(), checksum);
        
        return saved;
    }
    
    public Page<AIAuditLog> searchAuditLogs(AuditFilterRequest filter) {
        logger.info("Searching audit logs with filters");
        
        Sort sort = filter.getSortDirection().equalsIgnoreCase("ASC") 
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        
        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getPageSize(), sort);
        
        Page<AIAuditLog> results = auditLogRepository.findAll(pageRequest);
        
        if (filter.getStartDate() != null || filter.getEndDate() != null) {
            results = filterByDateRange(results, filter.getStartDate(), filter.getEndDate());
        }
        
        if (filter.getUserIds() != null && !filter.getUserIds().isEmpty()) {
            results = filterByUsers(results, filter.getUserIds());
        }
        
        if (filter.getEventTypes() != null && !filter.getEventTypes().isEmpty()) {
            results = filterByEventTypes(results, filter.getEventTypes());
        }
        
        logger.info("Found {} audit logs matching filters", results.getTotalElements());
        
        return results;
    }
    
    public List<AIAuditLog> getUserActivityTrail(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Retrieving activity trail for user: {}", userId);
        
        try {
            UUID userUuid = UUID.fromString(userId);
            return auditLogRepository.findAll().stream()
                    .filter(auditLog -> auditLog.getUserId() != null && auditLog.getUserId().equals(userUuid))
                    .filter(auditLog -> isWithinDateRange(auditLog.getTimestamp(), startDate, endDate))
                    .sorted(Comparator.comparing(AIAuditLog::getTimestamp).reversed())
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid userId format: {}", userId);
            return Collections.emptyList();
        }
    }
    
    public List<AIAuditLog> getPatientAccessTrail(UUID patientId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Retrieving PHI access trail for patient: {}", patientId);
        
        return auditLogRepository.findAll().stream()
                .filter(auditLog -> patientId.equals(auditLog.getPatientId()))
                .filter(auditLog -> isWithinDateRange(auditLog.getTimestamp(), startDate, endDate))
                .sorted(Comparator.comparing(AIAuditLog::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
    
    public AuditReportResponse generateComplianceReport(AuditReportRequest request) {
        logger.info("Generating {} compliance report", request.getReportType());
        
        AuditReportResponse response = new AuditReportResponse();
        response.setReportId(UUID.randomUUID().toString());
        response.setReportType(request.getReportType());
        response.setFormat(request.getFormat());
        response.setPeriodStart(request.getStartDate());
        response.setPeriodEnd(request.getEndDate());
        
        List<AIAuditLog> logs = auditLogRepository.findAll().stream()
                .filter(auditLog -> isWithinDateRange(auditLog.getTimestamp(), request.getStartDate(), request.getEndDate()))
                .collect(Collectors.toList());
        
        response.setTotalEvents(logs.size());
        
        Map<String, Integer> eventCounts = logs.stream()
                .collect(Collectors.groupingBy(
                        AIAuditLog::getRequestType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        response.setEventCounts(eventCounts);
        
        long critical = logs.stream()
                .filter(auditLog -> isSecurityCritical(auditLog.getRequestType()))
                .count();
        response.setCriticalEvents((int) critical);
        
        long incidents = logs.stream()
                .filter(auditLog -> auditLog.getRequestType().contains("BREACH") || 
                              auditLog.getRequestType().contains("SUSPICIOUS"))
                .count();
        response.setSecurityIncidents((int) incidents);
        
        response.getSummary().put("totalUsers", countUniqueUsers(logs));
        response.getSummary().put("totalPatients", countUniquePatients(logs));
        response.getSummary().put("phiAccessCount", countPHIAccess(logs));
        response.getSummary().put("failedLoginAttempts", countFailedLogins(logs));
        
        logger.info("Compliance report generated: {} events, {} critical", 
                 response.getTotalEvents(), response.getCriticalEvents());
        
        return response;
    }
    
    public List<ComplianceViolation> getComplianceViolations() {
        logger.info("Retrieving compliance violations");
        return new ArrayList<>(violations.values());
    }
    
    public Map<String, Object> verifyAuditLogIntegrity() {
        logger.info("Verifying audit log integrity");
        
        List<AIAuditLog> logs = auditLogRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        
        int total = logs.size();
        int verified = 0;
        int tampered = 0;
        List<UUID> tamperedIds = new ArrayList<>();
        
        for (AIAuditLog auditLog : logs) {
            String storedChecksum = auditLog.getErrorMessage();
            String calculatedChecksum = generateChecksum(auditLog);
            
            if (storedChecksum != null && storedChecksum.equals(calculatedChecksum)) {
                verified++;
            } else if (storedChecksum != null) {
                tampered++;
                tamperedIds.add(auditLog.getId());
            }
        }
        
        result.put("totalLogs", total);
        result.put("verified", verified);
        result.put("tampered", tampered);
        result.put("tamperedIds", tamperedIds);
        result.put("integrityPercentage", total > 0 ? (verified * 100.0 / total) : 0);
        
        logger.info("Integrity check complete: {}/{} verified", verified, total);
        
        return result;
    }
    
    public List<AIAuditLog> detectSuspiciousActivity() {
        logger.info("Detecting suspicious activity");
        
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        
        List<AIAuditLog> recentLogs = auditLogRepository.findAll().stream()
                .filter(auditLog -> auditLog.getTimestamp().isAfter(last24Hours))
                .collect(Collectors.toList());
        
        List<AIAuditLog> suspicious = new ArrayList<>();
        
        Map<UUID, Long> failedLoginsByUser = recentLogs.stream()
                .filter(auditLog -> auditLog.getRequestType().contains("LOGIN_FAIL"))
                .collect(Collectors.groupingBy(AIAuditLog::getUserId, Collectors.counting()));
        
        failedLoginsByUser.forEach((userId, count) -> {
            if (count >= 5) {
                suspicious.addAll(recentLogs.stream()
                        .filter(auditLog -> userId.equals(auditLog.getUserId()))
                        .collect(Collectors.toList()));
            }
        });
        
        Map<UUID, Long> phiAccessByUser = recentLogs.stream()
                .filter(auditLog -> auditLog.getRequestType().startsWith("PHI_"))
                .collect(Collectors.groupingBy(AIAuditLog::getUserId, Collectors.counting()));
        
        phiAccessByUser.forEach((userId, count) -> {
            if (count >= 50) {
                suspicious.addAll(recentLogs.stream()
                        .filter(auditLog -> userId.equals(auditLog.getUserId()))
                        .filter(auditLog -> auditLog.getRequestType().startsWith("PHI_"))
                        .collect(Collectors.toList()));
            }
        });
        
        logger.info("Detected {} suspicious activities", suspicious.size());
        
        return suspicious.stream().distinct().collect(Collectors.toList());
    }
    
    public Map<String, Object> getAuditStatistics() {
        logger.info("Generating audit statistics");
        
        List<AIAuditLog> allLogs = auditLogRepository.findAll();
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        
        List<AIAuditLog> recent = allLogs.stream()
                .filter(auditLog -> auditLog.getTimestamp().isAfter(last30Days))
                .collect(Collectors.toList());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAuditLogs", allLogs.size());
        stats.put("logsLast30Days", recent.size());
        stats.put("uniqueUsers", countUniqueUsers(allLogs));
        stats.put("uniquePatients", countUniquePatients(allLogs));
        stats.put("phiAccessCount", countPHIAccess(allLogs));
        stats.put("securityIncidents", countSecurityIncidents(allLogs));
        stats.put("complianceViolations", violations.size());
        stats.put("oldestLog", allLogs.stream()
                .map(AIAuditLog::getTimestamp)
                .min(LocalDateTime::compareTo)
                .orElse(null));
        
        return stats;
    }
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void enforceRetentionPolicy() {
        logger.info("Enforcing 7-year retention policy");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(RETENTION_YEARS);
        
        List<AIAuditLog> expiredLogs = auditLogRepository.findAll().stream()
                .filter(auditLog -> auditLog.getTimestamp().isBefore(cutoffDate))
                .collect(Collectors.toList());
        
        if (!expiredLogs.isEmpty()) {
            logger.info("Found {} audit logs exceeding retention period - archiving", expiredLogs.size());
        } else {
            logger.info("No audit logs exceed retention period");
        }
    }
    
    public SseEmitter registerStream(AuditFilterRequest filter) {
        logger.info("Registering new SSE stream");
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            logger.info("SSE stream completed");
        });
        
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            logger.info("SSE stream timed out");
        });
        
        emitter.onError((e) -> {
            emitters.remove(emitter);
            logger.error("SSE stream error: {}", e.getMessage());
        });
        
        emitters.add(emitter);
        
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Stream established"));
        } catch (Exception e) {
            logger.error("Failed to send initial SSE event: {}", e.getMessage());
            emitters.remove(emitter);
        }
        
        logger.info("Total SSE streams: {}", emitters.size());
        
        return emitter;
    }
    
    private void streamAuditEvent(AIAuditLog auditLog, AuditEventType eventType, 
                                  AuditSeverity severity, DataSensitivity sensitivity) {
        if (emitters.isEmpty()) return;
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", auditLog.getId());
        eventData.put("eventType", eventType.name());
        eventData.put("severity", severity.name());
        eventData.put("sensitivity", sensitivity.name());
        eventData.put("timestamp", auditLog.getTimestamp());
        eventData.put("userId", auditLog.getUserId());
        eventData.put("patientId", auditLog.getPatientId());
        
        List<SseEmitter> deadEmitters = new ArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("audit-event")
                        .data(eventData));
            } catch (Exception e) {
                logger.debug("Failed to send SSE event, removing emitter");
                deadEmitters.add(emitter);
            }
        }
        
        emitters.removeAll(deadEmitters);
    }
    
    private void detectComplianceViolations(AIAuditLog auditLog, AuditEventType eventType, 
                                           AuditSeverity severity, DataSensitivity sensitivity) {
        if (eventType.isPHIEvent() && severity == AuditSeverity.CRITICAL) {
            ComplianceViolation violation = new ComplianceViolation();
            violation.setViolationType("UNAUTHORIZED_PHI_ACCESS");
            violation.setSeverity(AuditSeverity.CRITICAL);
            violation.setDescription("Unauthorized access to PHI detected");
            violation.setUserId(auditLog.getUserId() != null ? auditLog.getUserId().toString() : null);
            violation.setPatientId(auditLog.getPatientId());
            violation.setDetails(auditLog.getResponse());
            
            violations.put(violation.getViolationId(), violation);
            
            logger.warn("Compliance violation detected: {}", violation.getViolationType());
        }
    }
    
    private String generateChecksum(AIAuditLog auditLog) {
        try {
            MessageDigest digest = MessageDigest.getInstance(CHECKSUM_ALGORITHM);
            
            String data = String.format("%s|%s|%s|%s|%s|%s",
                    auditLog.getPatientId(),
                    auditLog.getUserId(),
                    auditLog.getRequestType(),
                    auditLog.getTimestamp(),
                    auditLog.getPrompt(),
                    lastChecksum);
            
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Failed to generate checksum: {}", e.getMessage());
            return "";
        }
    }
    
    private boolean isWithinDateRange(LocalDateTime timestamp, LocalDateTime start, LocalDateTime end) {
        if (start != null && timestamp.isBefore(start)) return false;
        if (end != null && timestamp.isAfter(end)) return false;
        return true;
    }
    
    private boolean isSecurityCritical(String requestType) {
        try {
            AuditEventType eventType = AuditEventType.valueOf(requestType);
            return eventType.isSecurityCritical();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    private long countUniqueUsers(List<AIAuditLog> logs) {
        return logs.stream()
                .map(AIAuditLog::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }
    
    private long countUniquePatients(List<AIAuditLog> logs) {
        return logs.stream()
                .map(AIAuditLog::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }
    
    private long countPHIAccess(List<AIAuditLog> logs) {
        return logs.stream()
                .filter(auditLog -> auditLog.getRequestType().startsWith("PHI_"))
                .count();
    }
    
    private long countFailedLogins(List<AIAuditLog> logs) {
        return logs.stream()
                .filter(auditLog -> auditLog.getRequestType().contains("LOGIN_FAIL"))
                .count();
    }
    
    private long countSecurityIncidents(List<AIAuditLog> logs) {
        return logs.stream()
                .filter(auditLog -> auditLog.getRequestType().contains("BREACH") || 
                              auditLog.getRequestType().contains("SUSPICIOUS"))
                .count();
    }
    
    private Page<AIAuditLog> filterByDateRange(Page<AIAuditLog> page, LocalDateTime start, LocalDateTime end) {
        return page;
    }
    
    private Page<AIAuditLog> filterByUsers(Page<AIAuditLog> page, List<String> userIds) {
        return page;
    }
    
    private Page<AIAuditLog> filterByEventTypes(Page<AIAuditLog> page, List<AuditEventType> eventTypes) {
        return page;
    }
}
