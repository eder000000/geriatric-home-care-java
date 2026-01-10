package com.geriatriccare.dto.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AIAnalyticsResponse {
    
    private AIUsageMetrics overallMetrics;
    private List<FeatureUsageStats> featureUsageBreakdown;
    private List<String> topUsers;
    private List<String> recentErrors;
    private String costTrend;
    private String usageTrend;
    private LocalDateTime generatedAt;
    
    public AIAnalyticsResponse() {
        this.featureUsageBreakdown = new ArrayList<>();
        this.topUsers = new ArrayList<>();
        this.recentErrors = new ArrayList<>();
    }
    
    // Getters and Setters
    public AIUsageMetrics getOverallMetrics() { return overallMetrics; }
    public void setOverallMetrics(AIUsageMetrics overallMetrics) { this.overallMetrics = overallMetrics; }
    
    public List<FeatureUsageStats> getFeatureUsageBreakdown() { return featureUsageBreakdown; }
    public void setFeatureUsageBreakdown(List<FeatureUsageStats> featureUsageBreakdown) { this.featureUsageBreakdown = featureUsageBreakdown; }
    
    public List<String> getTopUsers() { return topUsers; }
    public void setTopUsers(List<String> topUsers) { this.topUsers = topUsers; }
    
    public List<String> getRecentErrors() { return recentErrors; }
    public void setRecentErrors(List<String> recentErrors) { this.recentErrors = recentErrors; }
    
    public String getCostTrend() { return costTrend; }
    public void setCostTrend(String costTrend) { this.costTrend = costTrend; }
    
    public String getUsageTrend() { return usageTrend; }
    public void setUsageTrend(String usageTrend) { this.usageTrend = usageTrend; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
