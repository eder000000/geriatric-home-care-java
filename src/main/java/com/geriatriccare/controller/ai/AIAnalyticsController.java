package com.geriatriccare.controller.ai;

import com.geriatriccare.dto.ai.AIAnalyticsResponse;
import com.geriatriccare.dto.ai.AIUsageMetrics;
import com.geriatriccare.dto.ai.FeatureUsageStats;
import com.geriatriccare.service.ai.AIAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ai/analytics")
public class AIAnalyticsController {
    
    private static final Logger log = LoggerFactory.getLogger(AIAnalyticsController.class);
    
    private final AIAnalyticsService analyticsService;
    
    @Autowired
    public AIAnalyticsController(AIAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AIAnalyticsResponse> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Analytics overview requested");
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        AIAnalyticsResponse response = analyticsService.getOverallAnalytics(startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AIUsageMetrics> getMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Metrics requested for period");
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        AIUsageMetrics metrics = analyticsService.getMetricsForPeriod(startDate, endDate);
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/features")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeatureUsageStats>> getFeatureUsage() {
        
        log.info("Feature usage stats requested");
        
        List<FeatureUsageStats> stats = analyticsService.getFeatureUsageStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/cost/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Double> getMonthlyCost() {
        
        log.info("Monthly cost estimate requested");
        
        Double cost = analyticsService.getEstimatedMonthlyCost();
        return ResponseEntity.ok(cost);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Analytics Service is running");
    }
}
