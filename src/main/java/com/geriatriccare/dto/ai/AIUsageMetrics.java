package com.geriatriccare.dto.ai;

import java.time.LocalDateTime;

public class AIUsageMetrics {
    
    private Long totalRequests;
    private Long successfulRequests;
    private Long failedRequests;
    private Double successRate;
    private Integer totalTokensUsed;
    private Double estimatedCost;
    private Double averageResponseTimeMs;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    public AIUsageMetrics() {
        this.totalRequests = 0L;
        this.successfulRequests = 0L;
        this.failedRequests = 0L;
        this.totalTokensUsed = 0;
        this.estimatedCost = 0.0;
    }
    
    // Getters and Setters
    public Long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(Long totalRequests) { this.totalRequests = totalRequests; }
    
    public Long getSuccessfulRequests() { return successfulRequests; }
    public void setSuccessfulRequests(Long successfulRequests) { this.successfulRequests = successfulRequests; }
    
    public Long getFailedRequests() { return failedRequests; }
    public void setFailedRequests(Long failedRequests) { this.failedRequests = failedRequests; }
    
    public Double getSuccessRate() { return successRate; }
    public void setSuccessRate(Double successRate) { this.successRate = successRate; }
    
    public Integer getTotalTokensUsed() { return totalTokensUsed; }
    public void setTotalTokensUsed(Integer totalTokensUsed) { this.totalTokensUsed = totalTokensUsed; }
    
    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }
    
    public Double getAverageResponseTimeMs() { return averageResponseTimeMs; }
    public void setAverageResponseTimeMs(Double averageResponseTimeMs) { this.averageResponseTimeMs = averageResponseTimeMs; }
    
    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
    
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
}
