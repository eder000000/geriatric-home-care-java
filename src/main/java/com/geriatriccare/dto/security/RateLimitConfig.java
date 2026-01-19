package com.geriatriccare.dto.security;

public class RateLimitConfig {
    
    private Integer requestsPerMinute;
    private Integer requestsPerHour;
    private Integer requestsPerDay;
    private Boolean enabled;
    
    public RateLimitConfig() {
        this.requestsPerMinute = 60;
        this.requestsPerHour = 1000;
        this.requestsPerDay = 10000;
        this.enabled = true;
    }
    
    // Getters and Setters
    public Integer getRequestsPerMinute() { return requestsPerMinute; }
    public void setRequestsPerMinute(Integer requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    
    public Integer getRequestsPerHour() { return requestsPerHour; }
    public void setRequestsPerHour(Integer requestsPerHour) { this.requestsPerHour = requestsPerHour; }
    
    public Integer getRequestsPerDay() { return requestsPerDay; }
    public void setRequestsPerDay(Integer requestsPerDay) { this.requestsPerDay = requestsPerDay; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
