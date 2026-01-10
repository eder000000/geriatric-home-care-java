package com.geriatriccare.dto.ai;

import java.time.LocalDateTime;
import java.util.UUID;

public class AIRecommendationReview {
    
    private UUID id;
    private UUID auditLogId;
    private UUID patientId;
    private String patientName;
    private String recommendationType;
    private String originalRecommendation;
    private ReviewStatus status;
    private ReviewAction action;
    private String reviewerComments;
    private String modifications;
    private String rejectionReason;
    private UUID reviewedBy;
    private String reviewerName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private Integer urgencyLevel;
    private Boolean notificationSent;
    
    public AIRecommendationReview() {
        this.status = ReviewStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.urgencyLevel = 2;
        this.notificationSent = false;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getAuditLogId() { return auditLogId; }
    public void setAuditLogId(UUID auditLogId) { this.auditLogId = auditLogId; }
    
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getRecommendationType() { return recommendationType; }
    public void setRecommendationType(String recommendationType) { this.recommendationType = recommendationType; }
    
    public String getOriginalRecommendation() { return originalRecommendation; }
    public void setOriginalRecommendation(String originalRecommendation) { this.originalRecommendation = originalRecommendation; }
    
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    
    public ReviewAction getAction() { return action; }
    public void setAction(ReviewAction action) { this.action = action; }
    
    public String getReviewerComments() { return reviewerComments; }
    public void setReviewerComments(String reviewerComments) { this.reviewerComments = reviewerComments; }
    
    public String getModifications() { return modifications; }
    public void setModifications(String modifications) { this.modifications = modifications; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }
    
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Integer getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(Integer urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    
    public Boolean getNotificationSent() { return notificationSent; }
    public void setNotificationSent(Boolean notificationSent) { this.notificationSent = notificationSent; }
}
