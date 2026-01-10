package com.geriatriccare.service.ai;

import com.geriatriccare.dto.ai.*;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.entity.Patient;
import com.geriatriccare.repository.AIAuditLogRepository;
import com.geriatriccare.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AIReviewService {
    
    private static final Logger log = LoggerFactory.getLogger(AIReviewService.class);
    
    // In-memory storage (in production, use database table)
    private final Map<UUID, AIRecommendationReview> reviewStore = new ConcurrentHashMap<>();
    
    private final AIAuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    
    @Autowired
    public AIReviewService(
            AIAuditLogRepository auditLogRepository,
            PatientRepository patientRepository) {
        this.auditLogRepository = auditLogRepository;
        this.patientRepository = patientRepository;
    }
    
    @Transactional
    public AIRecommendationReview submitForReview(UUID auditLogId, Integer urgencyLevel) {
        log.info("Submitting recommendation for review: {}", auditLogId);
        
        AIAuditLog auditLog = auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new RuntimeException("Audit log not found: " + auditLogId));
        
        Patient patient = patientRepository.findById(auditLog.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found: " + auditLog.getPatientId()));
        
        AIRecommendationReview review = new AIRecommendationReview();
        review.setId(UUID.randomUUID());
        review.setAuditLogId(auditLogId);
        review.setPatientId(patient.getId());
        review.setPatientName(patient.getFirstName() + " " + patient.getLastName());
        review.setRecommendationType(auditLog.getRequestType());
        review.setOriginalRecommendation(auditLog.getResponse());
        review.setUrgencyLevel(urgencyLevel != null ? urgencyLevel : 2);
        review.setStatus(ReviewStatus.PENDING);
        
        reviewStore.put(review.getId(), review);
        
        log.info("Review created: {} for patient: {}", review.getId(), patient.getId());
        
        return review;
    }
    
    @Transactional
    public AIRecommendationReview reviewRecommendation(UUID reviewId, ReviewRequest request) {
        log.info("Processing review: {} with action: {}", reviewId, request.getAction());
        
        AIRecommendationReview review = reviewStore.get(reviewId);
        if (review == null) {
            throw new RuntimeException("Review not found: " + reviewId);
        }
        
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new RuntimeException("Review already processed with status: " + review.getStatus());
        }
        
        review.setAction(request.getAction());
        review.setReviewerComments(request.getReviewerComments());
        review.setReviewedBy(getCurrentUserId());
        review.setReviewerName(getCurrentUserName());
        review.setReviewedAt(LocalDateTime.now());
        
        switch (request.getAction()) {
            case APPROVE:
                review.setStatus(ReviewStatus.APPROVED);
                break;
            case APPROVE_WITH_MODIFICATIONS:
                review.setStatus(ReviewStatus.APPROVED_WITH_MODIFICATIONS);
                review.setModifications(request.getModifications());
                break;
            case REJECT:
                review.setStatus(ReviewStatus.REJECTED);
                review.setRejectionReason(request.getRejectionReason());
                break;
            case REQUEST_MORE_INFO:
                review.setStatus(ReviewStatus.PENDING);
                break;
            case ESCALATE:
                review.setStatus(ReviewStatus.PENDING);
                review.setUrgencyLevel(3);
                break;
        }
        
        if (request.getSendNotification()) {
            sendReviewNotification(review);
        }
        
        reviewStore.put(reviewId, review);
        
        log.info("Review {} completed with status: {}", reviewId, review.getStatus());
        
        return review;
    }
    
    @Transactional(readOnly = true)
    public List<AIRecommendationReview> getPendingReviews() {
        log.info("Retrieving pending reviews");
        
        return reviewStore.values().stream()
                .filter(review -> review.getStatus() == ReviewStatus.PENDING)
                .sorted(Comparator.comparing(AIRecommendationReview::getUrgencyLevel).reversed()
                        .thenComparing(AIRecommendationReview::getCreatedAt))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AIRecommendationReview> getReviewsByPatient(UUID patientId) {
        log.info("Retrieving reviews for patient: {}", patientId);
        
        return reviewStore.values().stream()
                .filter(review -> review.getPatientId().equals(patientId))
                .sorted(Comparator.comparing(AIRecommendationReview::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public AIRecommendationReview getReviewById(UUID reviewId) {
        log.info("Retrieving review: {}", reviewId);
        
        AIRecommendationReview review = reviewStore.get(reviewId);
        if (review == null) {
            throw new RuntimeException("Review not found: " + reviewId);
        }
        
        return review;
    }
    
    @Transactional(readOnly = true)
    public ReviewSummary getReviewSummary() {
        log.info("Generating review summary");
        
        ReviewSummary summary = new ReviewSummary();
        
        List<AIRecommendationReview> allReviews = new ArrayList<>(reviewStore.values());
        
        summary.setTotalPending(allReviews.stream()
                .filter(r -> r.getStatus() == ReviewStatus.PENDING)
                .count());
        
        summary.setTotalApproved(allReviews.stream()
                .filter(r -> r.getStatus() == ReviewStatus.APPROVED)
                .count());
        
        summary.setTotalRejected(allReviews.stream()
                .filter(r -> r.getStatus() == ReviewStatus.REJECTED)
                .count());
        
        summary.setTotalModified(allReviews.stream()
                .filter(r -> r.getStatus() == ReviewStatus.APPROVED_WITH_MODIFICATIONS)
                .count());
        
        summary.setUrgentReviews(allReviews.stream()
                .filter(r -> r.getStatus() == ReviewStatus.PENDING && r.getUrgencyLevel() >= 3)
                .count());
        
        double avgHours = allReviews.stream()
                .filter(r -> r.getReviewedAt() != null)
                .mapToLong(r -> ChronoUnit.HOURS.between(r.getCreatedAt(), r.getReviewedAt()))
                .average()
                .orElse(0.0);
        
        summary.setAverageReviewTimeHours(avgHours);
        
        log.info("Summary: {} pending, {} approved, {} rejected", 
                 summary.getTotalPending(), summary.getTotalApproved(), summary.getTotalRejected());
        
        return summary;
    }
    
    @Transactional
    public void bulkApprove(List<UUID> reviewIds, String comments) {
        log.info("Bulk approving {} reviews", reviewIds.size());
        
        for (UUID reviewId : reviewIds) {
            ReviewRequest request = new ReviewRequest();
            request.setAction(ReviewAction.APPROVE);
            request.setReviewerComments(comments);
            request.setSendNotification(false);
            
            try {
                reviewRecommendation(reviewId, request);
            } catch (Exception e) {
                log.error("Failed to approve review {}: {}", reviewId, e.getMessage());
            }
        }
        
        log.info("Bulk approval completed");
    }
    
    private void sendReviewNotification(AIRecommendationReview review) {
        log.info("Sending notification for review: {} (status: {})", review.getId(), review.getStatus());
        // In production: integrate with notification service
        review.setNotificationSent(true);
    }
    
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            return UUID.randomUUID();
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
    
    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "System";
    }
}
