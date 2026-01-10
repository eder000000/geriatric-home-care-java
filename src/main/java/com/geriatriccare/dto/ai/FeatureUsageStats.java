package com.geriatriccare.dto.ai;

public class FeatureUsageStats {
    
    private String featureName;
    private Long usageCount;
    private Double percentageOfTotal;
    private Integer averageTokensPerRequest;
    private Double averageCostPerRequest;
    
    public FeatureUsageStats() {}
    
    public FeatureUsageStats(String featureName, Long usageCount) {
        this.featureName = featureName;
        this.usageCount = usageCount;
    }
    
    // Getters and Setters
    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
    
    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
    
    public Double getPercentageOfTotal() { return percentageOfTotal; }
    public void setPercentageOfTotal(Double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
    
    public Integer getAverageTokensPerRequest() { return averageTokensPerRequest; }
    public void setAverageTokensPerRequest(Integer averageTokensPerRequest) { this.averageTokensPerRequest = averageTokensPerRequest; }
    
    public Double getAverageCostPerRequest() { return averageCostPerRequest; }
    public void setAverageCostPerRequest(Double averageCostPerRequest) { this.averageCostPerRequest = averageCostPerRequest; }
}
