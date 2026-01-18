package com.geriatriccare.controller.security;

import com.geriatriccare.dto.security.*;
import com.geriatriccare.service.security.HIPAAAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/security/audit")
public class HIPAAAuditController {
    
    private static final Logger log = LoggerFactory.getLogger(HIPAAAuditController.class);
    
    private final HIPAAAuditService auditService;
    
    @Autowired
    public HIPAAAuditController(HIPAAAuditService auditService) {
        this.auditService = auditService;
    }
    
    @PostMapping("/log")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuditEvent> logEvent(@RequestBody AuditEvent event) {
        log.info("Manual audit event logging request");
        
        AuditEvent loggedEvent = auditService.logEvent(event);
        return ResponseEntity.ok(loggedEvent);
    }
    
    @PostMapping("/query")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<List<AuditEvent>> queryEvents(@RequestBody AuditFilter filter) {
        log.info("Audit query request");
        
        List<AuditEvent> events = auditService.queryAuditEvents(filter);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditReport> generateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "COMPREHENSIVE") String reportType) {
        
        log.info("Audit report generation request: {} from {} to {}", reportType, startDate, endDate);
        
        AuditReport report = auditService.generateReport(startDate, endDate, reportType);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/compliance-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceStatus> getComplianceStatus() {
        log.info("Compliance status check request");
        
        ComplianceStatus status = auditService.checkComplianceStatus();
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/verify-integrity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> verifyIntegrity() {
        log.info("Audit log integrity verification request");
        
        int tamperedCount = auditService.verifyLogIntegrity();
        
        if (tamperedCount == 0) {
            return ResponseEntity.ok("All audit logs verified - no tampering detected");
        } else {
            return ResponseEntity.status(500)
                    .body("WARNING: " + tamperedCount + " tampered audit log entries detected");
        }
    }
    
    @GetMapping("/verify/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> verifyEvent(@PathVariable UUID eventId) {
        log.info("Event integrity verification request: {}", eventId);
        
        boolean valid = auditService.verifyEventIntegrity(eventId);
        
        if (valid) {
            return ResponseEntity.ok("Event verified - no tampering detected");
        } else {
            return ResponseEntity.status(500).body("WARNING: Event has been tampered with");
        }
    }
    
    @GetMapping("/phi-access")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<List<AuditEvent>> getPHIAccessLog(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("PHI access log request for patient: {}", patientId);
        
        AuditFilter filter = new AuditFilter();
        filter.setPhiEventsOnly(true);
        filter.setPatientId(patientId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        
        List<AuditEvent> events = auditService.queryAuditEvents(filter);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/security-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditEvent>> getSecurityEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Security events query request");
        
        AuditFilter filter = new AuditFilter();
        filter.setSecurityEventsOnly(true);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        
        List<AuditEvent> events = auditService.queryAuditEvents(filter);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("HIPAA Audit Service is running");
    }
}
