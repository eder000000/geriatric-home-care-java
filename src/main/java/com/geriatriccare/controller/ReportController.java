package com.geriatriccare.controller;

import com.geriatriccare.dto.report.AdherenceReportRequest;
import com.geriatriccare.dto.report.AdherenceReportResponse;
import com.geriatriccare.dto.report.AdherenceStatistics;
import com.geriatriccare.service.AdherenceReportService;
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
 * ReportController
 * REST API for care plan adherence reports
 * Sprint 8 - US-7.1 (GCARE-713)
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final AdherenceReportService reportService;

    @PostMapping("/adherence")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER')")
    public ResponseEntity<AdherenceReportResponse> generateAdherenceReport(
            @Valid @RequestBody AdherenceReportRequest request) {
        log.info("POST /api/reports/adherence - Generating report for patient: {}", request.getPatientId());
        AdherenceReportResponse report = reportService.generateReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<AdherenceReportResponse> getReport(@PathVariable UUID reportId) {
        log.info("GET /api/reports/{}", reportId);
        return ResponseEntity.ok(reportService.getReport(reportId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<List<AdherenceReportResponse>> getReportsByPatient(
            @PathVariable UUID patientId) {
        log.info("GET /api/reports/patient/{}", patientId);
        return ResponseEntity.ok(reportService.getReportsByPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<Page<AdherenceReportResponse>> getReportsByPatientPaginated(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/reports/patient/{}/paginated?page={}&size={}", patientId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reportService.getReportsByPatientPaginated(patientId, pageable));
    }

    @GetMapping("/patient/{patientId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN', 'CAREGIVER', 'FAMILY')")
    public ResponseEntity<AdherenceStatistics> getAdherenceStatistics(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/reports/patient/{}/statistics?days={}", patientId, days);
        return ResponseEntity.ok(reportService.getAdherenceStatistics(patientId, days));
    }
}
