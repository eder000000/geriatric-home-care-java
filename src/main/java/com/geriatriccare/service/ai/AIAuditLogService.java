package com.geriatriccare.service.ai;

import com.geriatriccare.dto.ai.OpenAIResponse;
import com.geriatriccare.entity.AIAuditLog;
import com.geriatriccare.repository.AIAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AIAuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AIAuditLogService.class);

    private final AIAuditLogRepository auditLogRepository;

    public AIAuditLogService(AIAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAIRequest(String prompt, OpenAIResponse response) {
        AIAuditLog auditLog = new AIAuditLog();
        auditLog.setRequestType("AI_COMPLETION");
        auditLog.setPrompt(truncate(prompt, 5000)); // Truncate for storage
        auditLog.setSuccess(true);
        
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            String responseText = response.getChoices().get(0).getMessage().getContent();
            auditLog.setResponse(truncate(responseText, 10000));
            
            if (response.getUsage() != null) {
                auditLog.setTokensUsed(response.getUsage().getTotalTokens());
            }
        }
        
        auditLogRepository.save(auditLog);
        log.debug("AI request logged with ID: {}", auditLog.getId());
    }

    @Transactional
    public void logAIError(String prompt, String errorMessage) {
        AIAuditLog auditLog = new AIAuditLog();
        auditLog.setRequestType("AI_COMPLETION");
        auditLog.setPrompt(truncate(prompt, 5000));
        auditLog.setSuccess(false);
        auditLog.setErrorMessage(errorMessage);
        
        auditLogRepository.save(auditLog);
        log.warn("AI error logged: {}", errorMessage);
    }

    @Transactional
    public void approveRecommendation(UUID auditLogId, UUID approvedBy) {
        AIAuditLog auditLog = auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new RuntimeException("Audit log not found"));
        
        auditLog.setApproved(true);
        auditLog.setApprovedBy(approvedBy);
        auditLog.setApprovedAt(LocalDateTime.now());
        
        auditLogRepository.save(auditLog);
        log.info("AI recommendation {} approved by user {}", auditLogId, approvedBy);
    }

    public List<AIAuditLog> getPendingApprovals() {
        return auditLogRepository.findByApprovedFalse();
    }

    public List<AIAuditLog> getUserAuditLogs(UUID userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<AIAuditLog> getPatientAuditLogs(UUID patientId) {
        return auditLogRepository.findByPatientIdOrderByTimestampDesc(patientId);
    }

    public Double getAverageResponseTime() {
        return auditLogRepository.getAverageResponseTime();
    }

    public Long getTotalTokensUsed() {
        return auditLogRepository.getTotalTokensUsed();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}