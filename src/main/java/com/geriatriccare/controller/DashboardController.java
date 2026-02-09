package com.geriatriccare.controller;

import com.geriatriccare.dto.dashboard.DashboardRequest;
import com.geriatriccare.dto.dashboard.DashboardStatistics;
import com.geriatriccare.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * DashboardController
 * REST API for health outcome dashboards
 * Sprint 8 - US-7.3 (GCARE-734)
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @PostMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN')")
    public ResponseEntity<DashboardStatistics> getDashboardStatistics(
            @RequestBody(required = false) DashboardRequest request) {
        log.info("POST /api/dashboard/statistics");
        
        if (request == null) {
            request = new DashboardRequest();
        }
        
        DashboardStatistics stats = dashboardService.generateDashboard(request);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHYSICIAN')")
    public ResponseEntity<DashboardStatistics> getDashboardStatisticsSimple() {
        log.info("GET /api/dashboard/statistics");
        DashboardStatistics stats = dashboardService.generateDashboard(new DashboardRequest());
        return ResponseEntity.ok(stats);
    }
}
