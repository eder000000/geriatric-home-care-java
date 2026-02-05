package com.geriatriccare.entity;

import com.geriatriccare.enums.AlertSeverity;
import com.geriatriccare.enums.ComparisonOperator;
import com.geriatriccare.enums.VitalSignType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_rules", indexes = {
    @Index(name = "idx_alert_rule_patient", columnList = "patient_id"),
    @Index(name = "idx_alert_rule_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "patient_id")
    private UUID patientId; // null = global rule

    @NotNull(message = "Vital sign type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "vital_sign_type", nullable = false, length = 30)
    private VitalSignType vitalSignType;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AlertSeverity severity;

    @NotNull(message = "Comparison operator is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_operator", nullable = false, length = 20)
    private ComparisonOperator comparisonOperator;

    @NotNull(message = "Threshold value is required")
    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue;

    @Column(name = "threshold_value_max")
    private Double thresholdValueMax; // For BETWEEN operator

    @NotBlank(message = "Alert message is required")
    @Column(name = "alert_message", nullable = false, length = 500)
    private String alertMessage;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "cooldown_minutes")
    private Integer cooldownMinutes = 30; // Prevent duplicate alerts

    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
