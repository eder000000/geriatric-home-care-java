package com.geriatriccare.controller;

import com.geriatriccare.dto.medication.MedicationAdherenceReportRequest;
import com.geriatriccare.dto.medication.MedicationAdherenceReportResponse;
import com.geriatriccare.dto.medication.MedicationAdherenceStatistics;
import com.geriatriccare.service.MedicationAdherenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * MedicationReportController
 * REST API for medication adherence reports
 * Sprint 8 - US-7.2 (GCARE-723)
 */
@RestController
@RequestMapping("/api/reports/medication")
@RequiredArgsConstructor
@Slf4j
public class MedicationReportController {

    private final MedicationAdherenceService reportService;

    @PostMapping("/adherence")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'NURSE', 'CAREGIVER')")
    public ResponseEntity<MedicationAdherenceReportResponse> generateMedicationAdherenceReport(
            @Valid @RequestBody MedicationAdherenceReportRequest request) {
        log.info("POST /api/reports/medication/adherence - Patient: {}", request.getPatientId());
        MedicationAdherenceReportResponse report = reportService.generateReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'NURSE', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<MedicationAdherenceReportResponse> getReport(@PathVariable UUID reportId) {
        log.info("GET /api/reports/medication/{}", reportId);
        return ResponseEntity.ok(reportService.getReport(reportId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'NURSE', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<List<MedicationAdherenceReportResponse>> getReportsByPatient(
            @PathVariable UUID patientId) {
        log.info("GET /api/reports/medication/patient/{}", patientId);
        return ResponseEntity.ok(reportService.getReportsByPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'NURSE', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<Page<MedicationAdherenceReportResponse>> getReportsByPatientPaginated(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/reports/medication/patient/{}/paginated?page={}&size={}", patientId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reportService.getReportsByPatientPaginated(patientId, pageable));
    }

    @GetMapping("/patient/{patientId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'NURSE', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<MedicationAdherenceStatistics> getAdherenceStatistics(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/reports/medication/patient/{}/statistics?days={}", patientId, days);
        return ResponseEntity.ok(reportService.getAdherenceStatistics(patientId, days));
    }
}
