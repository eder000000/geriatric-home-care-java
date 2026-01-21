package com.geriatriccare.controller.security;

import com.geriatriccare.dto.security.*;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.enums.AuditEventType;
import com.geriatriccare.enums.AuditSeverity;
import com.geriatriccare.service.security.AuditEnhancementService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;




@RestController
@RequestMapping("/api/audit")
public class AuditEnhancementController {
    
    private static final Logger log = LoggerFactory.getLogger(AuditEnhancementController.class);
    
    private final AuditEnhancementService auditService;
    
    @Autowired
    public AuditEnhancementController(AuditEnhancementService auditService) {
        this.auditService = auditService;
    }
    
    @PostMapping("/log")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<AIAuditLog> logAuditEvent(
            @RequestParam AuditEventType eventType,
            @RequestParam AuditSeverity severity,
            @RequestParam DataSensitivity dataSensitivity,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String details,
            @RequestParam(required = false) String ipAddress) {
        
        log.info("Manual audit log request: {}", eventType);
        
        AIAuditLog auditLog = auditService.logEnhancedAuditEvent(
                eventType, severity, dataSensitivity, patientId, userId, details, ipAddress);
        
        return ResponseEntity.ok(auditLog);
    }
    
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<Page<AIAuditLog>> searchAuditLogs(@RequestBody AuditFilterRequest filter) {
        
        log.info("Audit search request");
        
        Page<AIAuditLog> results = auditService.searchAuditLogs(filter);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<List<AIAuditLog>> getUserActivityTrail(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("User activity trail request for: {}", userId);
        
        List<AIAuditLog> trail = auditService.getUserActivityTrail(userId, startDate, endDate);
        return ResponseEntity.ok(trail);
    }
    
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<List<AIAuditLog>> getPatientAccessTrail(
            @PathVariable UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Patient access trail request for: {}", patientId);
        
        List<AIAuditLog> trail = auditService.getPatientAccessTrail(patientId, startDate, endDate);
        return ResponseEntity.ok(trail);
    }
    
    @PostMapping("/reports/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<AuditReportResponse> generateReport(@RequestBody AuditReportRequest request) {
        
        log.info("Compliance report generation request: {}", request.getReportType());
        
        AuditReportResponse report = auditService.generateComplianceReport(request);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/reports/compliance")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<AuditReportResponse> getComplianceSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Compliance summary request");
        
        AuditReportRequest request = new AuditReportRequest();
        request.setReportType("COMPLIANCE");
        request.setStartDate(startDate != null ? startDate : LocalDateTime.now().minusDays(30));
        request.setEndDate(endDate != null ? endDate : LocalDateTime.now());
        
        AuditReportResponse report = auditService.generateComplianceReport(request);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/violations")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<List<ComplianceViolation>> getComplianceViolations() {
        
        log.info("Compliance violations request");
        
        List<ComplianceViolation> violations = auditService.getComplianceViolations();
        return ResponseEntity.ok(violations);
    }
    
    @PostMapping("/verify-integrity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> verifyIntegrity() {
        
        log.info("Audit log integrity verification request");
        
        Map<String, Object> result = auditService.verifyAuditLogIntegrity();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/suspicious-activity")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<List<AIAuditLog>> getSuspiciousActivity() {
        
        log.info("Suspicious activity detection request");
        
        List<AIAuditLog> suspicious = auditService.detectSuspiciousActivity();
        return ResponseEntity.ok(suspicious);
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        
        log.info("Audit statistics request");
        
        Map<String, Object> stats = auditService.getAuditStatistics();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportAuditLogs(
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Audit log export request: {}", format);
        
        // Placeholder - implement actual export logic
        String exportData = "Export functionality - " + format;
        
        return ResponseEntity.ok(exportData);
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public SseEmitter streamAuditEvents(@RequestBody(required = false) AuditFilterRequest filter) {
        
        log.info("Real-time audit event stream request");
        
        if (filter == null) {
            filter = new AuditFilterRequest();
        }
        
        return auditService.registerStream(filter);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Audit Enhancement Service is running");
    }
}
