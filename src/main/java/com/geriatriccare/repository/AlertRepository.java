package com.geriatriccare.repository;

import com.geriatriccare.entity.Alert;
import com.geriatriccare.enums.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByPatientIdAndStatusOrderByTriggeredAtDesc(UUID patientId, AlertStatus status);

    Page<Alert> findByPatientIdOrderByTriggeredAtDesc(UUID patientId, Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.patientId = :patientId AND a.status IN :statuses ORDER BY a.triggeredAt DESC")
    List<Alert> findByPatientIdAndStatuses(
        @Param("patientId") UUID patientId,
        @Param("statuses") List<AlertStatus> statuses
    );

    @Query("SELECT a FROM Alert a WHERE a.vitalSignId = :vitalSignId AND a.triggeredRuleId = :ruleId AND a.triggeredAt > :after")
    List<Alert> findRecentSimilarAlerts(
        @Param("vitalSignId") UUID vitalSignId,
        @Param("ruleId") UUID ruleId,
        @Param("after") LocalDateTime after
    );

    long countByPatientIdAndStatus(UUID patientId, AlertStatus status);
}
