package com.geriatriccare.dto.ai;

public class ReviewSummary {
    
    private Long totalPending;
    private Long totalApproved;
    private Long totalRejected;
    private Long totalModified;
    private Long urgentReviews;
    private Double averageReviewTimeHours;
    
    public ReviewSummary() {
        this.totalPending = 0L;
        this.totalApproved = 0L;
        this.totalRejected = 0L;
        this.totalModified = 0L;
        this.urgentReviews = 0L;
    }
    
    // Getters and Setters
    public Long getTotalPending() { return totalPending; }
    public void setTotalPending(Long totalPending) { this.totalPending = totalPending; }
    
    public Long getTotalApproved() { return totalApproved; }
    public void setTotalApproved(Long totalApproved) { this.totalApproved = totalApproved; }
    
    public Long getTotalRejected() { return totalRejected; }
    public void setTotalRejected(Long totalRejected) { this.totalRejected = totalRejected; }
    
    public Long getTotalModified() { return totalModified; }
    public void setTotalModified(Long totalModified) { this.totalModified = totalModified; }
    
    public Long getUrgentReviews() { return urgentReviews; }
    public void setUrgentReviews(Long urgentReviews) { this.urgentReviews = urgentReviews; }
    
    public Double getAverageReviewTimeHours() { return averageReviewTimeHours; }
    public void setAverageReviewTimeHours(Double averageReviewTimeHours) { this.averageReviewTimeHours = averageReviewTimeHours; }
}
