package com.geriatriccare.controller;

import com.geriatriccare.dto.alert.AlertRuleRequest;
import com.geriatriccare.dto.alert.AlertRuleResponse;
import com.geriatriccare.service.AlertRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alert-rules")
@RequiredArgsConstructor
@Slf4j
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN')")
    public ResponseEntity<AlertRuleResponse> createAlertRule(@Valid @RequestBody AlertRuleRequest request) {
        log.info("POST /api/alert-rules");
        return ResponseEntity.status(HttpStatus.CREATED).body(alertRuleService.createAlertRule(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER')")
    public ResponseEntity<AlertRuleResponse> getAlertRule(@PathVariable UUID id) {
        log.info("GET /api/alert-rules/{}", id);
        return ResponseEntity.ok(alertRuleService.getAlertRule(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER')")
    public ResponseEntity<List<AlertRuleResponse>> getAllAlertRules() {
        log.info("GET /api/alert-rules");
        return ResponseEntity.ok(alertRuleService.getAllAlertRules());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER')")
    public ResponseEntity<List<AlertRuleResponse>> getActiveRules() {
        log.info("GET /api/alert-rules/active");
        return ResponseEntity.ok(alertRuleService.getActiveRules());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER')")
    public ResponseEntity<List<AlertRuleResponse>> getRulesForPatient(@PathVariable UUID patientId) {
        log.info("GET /api/alert-rules/patient/{}", patientId);
        return ResponseEntity.ok(alertRuleService.getRulesForPatient(patientId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN')")
    public ResponseEntity<AlertRuleResponse> updateAlertRule(
            @PathVariable UUID id,
            @Valid @RequestBody AlertRuleRequest request) {
        log.info("PUT /api/alert-rules/{}", id);
        return ResponseEntity.ok(alertRuleService.updateAlertRule(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAlertRule(@PathVariable UUID id) {
        log.info("DELETE /api/alert-rules/{}", id);
        alertRuleService.deleteAlertRule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN')")
    public ResponseEntity<Void> deactivateAlertRule(@PathVariable UUID id) {
        log.info("POST /api/alert-rules/{}/deactivate", id);
        alertRuleService.deactivateAlertRule(id);
        return ResponseEntity.noContent().build();
    }
}
