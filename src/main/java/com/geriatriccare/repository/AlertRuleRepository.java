package com.geriatriccare.repository;

import com.geriatriccare.entity.AlertRule;
import com.geriatriccare.enums.VitalSignType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {

    List<AlertRule> findByPatientIdAndIsActiveTrue(UUID patientId);

    @Query("SELECT r FROM AlertRule r WHERE r.patientId IS NULL AND r.isActive = true")
    List<AlertRule> findGlobalActiveRules();

    @Query("SELECT r FROM AlertRule r WHERE (r.patientId = :patientId OR r.patientId IS NULL) AND r.vitalSignType = :type AND r.isActive = true")
    List<AlertRule> findApplicableRules(
        @Param("patientId") UUID patientId,
        @Param("type") VitalSignType type
    );

    List<AlertRule> findByIsActiveTrue();
}
