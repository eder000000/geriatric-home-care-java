package com.geriatriccare.dto.ai;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ReviewRequest {
    
    @NotNull(message = "Audit log ID is required")
    private UUID auditLogId;
    
    @NotNull(message = "Review action is required")
    private ReviewAction action;
    
    private String reviewerComments;
    private String modifications;
    private String rejectionReason;
    private Boolean sendNotification;
    
    public ReviewRequest() {
        this.sendNotification = true;
    }
    
    // Getters and Setters
    public UUID getAuditLogId() { return auditLogId; }
    public void setAuditLogId(UUID auditLogId) { this.auditLogId = auditLogId; }
    
    public ReviewAction getAction() { return action; }
    public void setAction(ReviewAction action) { this.action = action; }
    
    public String getReviewerComments() { return reviewerComments; }
    public void setReviewerComments(String reviewerComments) { this.reviewerComments = reviewerComments; }
    
    public String getModifications() { return modifications; }
    public void setModifications(String modifications) { this.modifications = modifications; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public Boolean getSendNotification() { return sendNotification; }
    public void setSendNotification(Boolean sendNotification) { this.sendNotification = sendNotification; }
}
