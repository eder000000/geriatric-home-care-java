package com.geriatriccare.controller.ai;

import com.geriatriccare.dto.ai.AIRecommendationReview;
import com.geriatriccare.dto.ai.ReviewRequest;
import com.geriatriccare.dto.ai.ReviewSummary;
import com.geriatriccare.service.ai.AIReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai/reviews")
public class AIReviewController {
    
    private static final Logger log = LoggerFactory.getLogger(AIReviewController.class);
    
    private final AIReviewService reviewService;
    
    @Autowired
    public AIReviewController(AIReviewService reviewService) {
        this.reviewService = reviewService;
    }
    
    @PostMapping("/submit/{auditLogId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<AIRecommendationReview> submitForReview(
            @PathVariable UUID auditLogId,
            @RequestParam(required = false, defaultValue = "2") Integer urgencyLevel) {
        
        log.info("Submit for review request: {}", auditLogId);
        
        try {
            AIRecommendationReview review = reviewService.submitForReview(auditLogId, urgencyLevel);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            log.error("Failed to submit for review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{reviewId}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<AIRecommendationReview> reviewRecommendation(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request) {
        
        log.info("Review action request for: {}", reviewId);
        
        try {
            AIRecommendationReview review = reviewService.reviewRecommendation(reviewId, request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            log.error("Failed to process review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<List<AIRecommendationReview>> getPendingReviews() {
        
        log.info("Retrieving pending reviews");
        
        List<AIRecommendationReview> reviews = reviewService.getPendingReviews();
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<List<AIRecommendationReview>> getPatientReviews(@PathVariable UUID patientId) {
        
        log.info("Retrieving reviews for patient: {}", patientId);
        
        List<AIRecommendationReview> reviews = reviewService.getReviewsByPatient(patientId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER', 'OWNER')")
    public ResponseEntity<AIRecommendationReview> getReview(@PathVariable UUID reviewId) {
        
        log.info("Retrieving review: {}", reviewId);
        
        try {
            AIRecommendationReview review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            log.error("Failed to retrieve review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewSummary> getReviewSummary() {
        
        log.info("Generating review summary");
        
        ReviewSummary summary = reviewService.getReviewSummary();
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping("/bulk-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> bulkApprove(
            @RequestBody List<UUID> reviewIds,
            @RequestParam(required = false) String comments) {
        
        log.info("Bulk approve request for {} reviews", reviewIds.size());
        
        try {
            reviewService.bulkApprove(reviewIds, comments);
            return ResponseEntity.ok("Bulk approval completed");
        } catch (Exception e) {
            log.error("Bulk approval failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Review Service is running");
    }
}
