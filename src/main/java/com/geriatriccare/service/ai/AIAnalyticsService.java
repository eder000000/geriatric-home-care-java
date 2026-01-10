package com.geriatriccare.service.ai;

import com.geriatriccare.dto.ai.*;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.repository.AIAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIAnalyticsService {
    
    private static final Logger log = LoggerFactory.getLogger(AIAnalyticsService.class);
    private static final double COST_PER_1K_TOKENS = 0.03;
    
    private final AIAuditLogRepository auditLogRepository;
    
    @Autowired
    public AIAnalyticsService(AIAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Transactional(readOnly = true)
    public AIAnalyticsResponse getOverallAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating analytics report from {} to {}", startDate, endDate);
        
        List<AIAuditLog> logs = auditLogRepository.findAll().stream()
                .filter(log -> log.getTimestamp().isAfter(startDate) && log.getTimestamp().isBefore(endDate))
                .collect(Collectors.toList());
        
        AIAnalyticsResponse response = new AIAnalyticsResponse();
        
        AIUsageMetrics metrics = calculateOverallMetrics(logs, startDate, endDate);
        response.setOverallMetrics(metrics);
        
        List<FeatureUsageStats> featureStats = calculateFeatureUsage(logs);
        response.setFeatureUsageBreakdown(featureStats);
        
        List<String> recentErrors = logs.stream()
                .filter(log -> !log.getSuccess())
                .limit(10)
                .map(log -> log.getRequestType() + ": " + log.getErrorMessage())
                .collect(Collectors.toList());
        response.setRecentErrors(recentErrors);
        
        response.setCostTrend(calculateCostTrend(logs));
        response.setUsageTrend(calculateUsageTrend(logs));
        response.setGeneratedAt(LocalDateTime.now());
        
        log.info("Analytics generated: {} total requests, {} tokens used", 
                 metrics.getTotalRequests(), metrics.getTotalTokensUsed());
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public AIUsageMetrics getMetricsForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating metrics for period: {} to {}", startDate, endDate);
        
        List<AIAuditLog> logs = auditLogRepository.findAll().stream()
                .filter(log -> log.getTimestamp().isAfter(startDate) && log.getTimestamp().isBefore(endDate))
                .collect(Collectors.toList());
        
        return calculateOverallMetrics(logs, startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public List<FeatureUsageStats> getFeatureUsageStats() {
        log.info("Calculating feature usage statistics");
        
        List<AIAuditLog> logs = auditLogRepository.findAll();
        return calculateFeatureUsage(logs);
    }
    
    @Transactional(readOnly = true)
    public Double getEstimatedMonthlyCost() {
        log.info("Calculating estimated monthly cost");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        List<AIAuditLog> logs = auditLogRepository.findAll().stream()
                .filter(log -> log.getTimestamp().isAfter(monthStart))
                .collect(Collectors.toList());
        
        int totalTokens = logs.stream()
                .filter(log -> log.getTokensUsed() != null)
                .mapToInt(AIAuditLog::getTokensUsed)
                .sum();
        
        double cost = (totalTokens / 1000.0) * COST_PER_1K_TOKENS;
        
        log.info("Monthly cost estimate: ${} ({} tokens)", String.format("%.2f", cost), totalTokens);
        
        return cost;
    }
    
    private AIUsageMetrics calculateOverallMetrics(List<AIAuditLog> logs, LocalDateTime start, LocalDateTime end) {
        AIUsageMetrics metrics = new AIUsageMetrics();
        
        metrics.setPeriodStart(start);
        metrics.setPeriodEnd(end);
        metrics.setTotalRequests((long) logs.size());
        
        long successful = logs.stream().filter(AIAuditLog::getSuccess).count();
        long failed = logs.size() - successful;
        
        metrics.setSuccessfulRequests(successful);
        metrics.setFailedRequests(failed);
        metrics.setSuccessRate(logs.size() > 0 ? (successful * 100.0 / logs.size()) : 0.0);
        
        int totalTokens = logs.stream()
                .filter(log -> log.getTokensUsed() != null)
                .mapToInt(AIAuditLog::getTokensUsed)
                .sum();
        
        metrics.setTotalTokensUsed(totalTokens);
        metrics.setEstimatedCost((totalTokens / 1000.0) * COST_PER_1K_TOKENS);
        
        double avgResponseTime = logs.stream()
                .filter(log -> log.getResponseTimeMs() != null)
                .mapToLong(AIAuditLog::getResponseTimeMs)
                .average()
                .orElse(0.0);
        
        metrics.setAverageResponseTimeMs(avgResponseTime);
        
        return metrics;
    }
    
    private List<FeatureUsageStats> calculateFeatureUsage(List<AIAuditLog> logs) {
        Map<String, Long> featureCounts = logs.stream()
                .collect(Collectors.groupingBy(AIAuditLog::getRequestType, Collectors.counting()));
        
        long total = logs.size();
        
        return featureCounts.entrySet().stream()
                .map(entry -> {
                    FeatureUsageStats stats = new FeatureUsageStats(entry.getKey(), entry.getValue());
                    stats.setPercentageOfTotal(total > 0 ? (entry.getValue() * 100.0 / total) : 0.0);
                    
                    double avgTokensDouble = logs.stream()
                            .filter(log -> log.getRequestType().equals(entry.getKey()))
                            .filter(log -> log.getTokensUsed() != null)
                            .mapToInt(AIAuditLog::getTokensUsed)
                            .average()
                            .orElse(0.0);
                    
                    int avgTokens = (int) avgTokensDouble;
                    
                    stats.setAverageTokensPerRequest(avgTokens);
                    stats.setAverageCostPerRequest((avgTokens / 1000.0) * COST_PER_1K_TOKENS);
                    
                    return stats;
                })
                .sorted(Comparator.comparing(FeatureUsageStats::getUsageCount).reversed())
                .collect(Collectors.toList());
    }
    
    private String calculateCostTrend(List<AIAuditLog> logs) {
        if (logs.size() < 7) return "Insufficient data";
        
        int midpoint = logs.size() / 2;
        
        int firstHalfTokens = logs.subList(0, midpoint).stream()
                .filter(log -> log.getTokensUsed() != null)
                .mapToInt(AIAuditLog::getTokensUsed)
                .sum();
        
        int secondHalfTokens = logs.subList(midpoint, logs.size()).stream()
                .filter(log -> log.getTokensUsed() != null)
                .mapToInt(AIAuditLog::getTokensUsed)
                .sum();
        
        if (secondHalfTokens > firstHalfTokens * 1.2) {
            return "Increasing (↑ " + String.format("%.0f%%", ((secondHalfTokens - firstHalfTokens) * 100.0 / firstHalfTokens)) + ")";
        } else if (secondHalfTokens < firstHalfTokens * 0.8) {
            return "Decreasing (↓ " + String.format("%.0f%%", ((firstHalfTokens - secondHalfTokens) * 100.0 / firstHalfTokens)) + ")";
        } else {
            return "Stable";
        }
    }
    
    private String calculateUsageTrend(List<AIAuditLog> logs) {
        if (logs.size() < 7) return "Insufficient data";
        
        int midpoint = logs.size() / 2;
        int firstHalf = midpoint;
        int secondHalf = logs.size() - midpoint;
        
        if (secondHalf > firstHalf * 1.2) {
            return "Increasing";
        } else if (secondHalf < firstHalf * 0.8) {
            return "Decreasing";
        } else {
            return "Stable";
        }
    }
}
