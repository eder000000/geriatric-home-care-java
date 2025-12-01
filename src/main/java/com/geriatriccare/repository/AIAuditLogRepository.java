package com.geriatriccare.repository;

import com.geriatriccare.entity.AIAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AIAuditLogRepository extends JpaRepository<AIAuditLog, UUID> {
    
    List<AIAuditLog> findByUserIdOrderByTimestampDesc(UUID userId);
    
    List<AIAuditLog> findByPatientIdOrderByTimestampDesc(UUID patientId);
    
    List<AIAuditLog> findByApprovedFalse();
    
    List<AIAuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT al FROM AIAuditLog al WHERE al.success = false ORDER BY al.timestamp DESC")
    List<AIAuditLog> findFailedRequests();
    
    @Query("SELECT AVG(al.responseTimeMs) FROM AIAuditLog al WHERE al.success = true")
    Double getAverageResponseTime();
    
    @Query("SELECT SUM(al.tokensUsed) FROM AIAuditLog al")
    Long getTotalTokensUsed();
}