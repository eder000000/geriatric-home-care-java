package com.geriatriccare.controller;

import com.geriatriccare.dto.alert.AlertResponse;
import com.geriatriccare.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<AlertResponse> getAlert(@PathVariable UUID id) {
        log.info("GET /api/alerts/{}", id);
        return ResponseEntity.ok(alertService.getAlert(id));
    }

    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts(@PathVariable UUID patientId) {
        log.info("GET /api/alerts/patient/{}/active", patientId);
        return ResponseEntity.ok(alertService.getActiveAlerts(patientId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<List<AlertResponse>> getAlertsByPatient(@PathVariable UUID patientId) {
        log.info("GET /api/alerts/patient/{}", patientId);
        return ResponseEntity.ok(alertService.getAlertsByPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<Page<AlertResponse>> getAlertsByPatientPaginated(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/alerts/patient/{}/paginated?page={}&size={}", patientId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(alertService.getAlertsByPatientPaginated(patientId, pageable));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER')")
    public ResponseEntity<AlertResponse> acknowledgeAlert(@PathVariable UUID id) {
        log.info("POST /api/alerts/{}/acknowledge", id);
        return ResponseEntity.ok(alertService.acknowledgeAlert(id));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER')")
    public ResponseEntity<AlertResponse> resolveAlert(
            @PathVariable UUID id,
            @RequestParam(required = false) String notes) {
        log.info("POST /api/alerts/{}/resolve", id);
        return ResponseEntity.ok(alertService.resolveAlert(id, notes));
    }

    @GetMapping("/patient/{patientId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<Long> getActiveAlertCount(@PathVariable UUID patientId) {
        log.info("GET /api/alerts/patient/{}/count", patientId);
        return ResponseEntity.ok(alertService.getActiveAlertCount(patientId));
    }
}
