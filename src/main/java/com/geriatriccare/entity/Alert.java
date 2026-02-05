package com.geriatriccare.entity;

import com.geriatriccare.enums.AlertSeverity;
import com.geriatriccare.enums.AlertStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_patient", columnList = "patient_id"),
    @Index(name = "idx_alert_status", columnList = "status"),
    @Index(name = "idx_alert_triggered", columnList = "triggered_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @NotNull(message = "Vital sign ID is required")
    @Column(name = "vital_sign_id", nullable = false)
    private UUID vitalSignId;

    @NotNull(message = "Alert rule ID is required")
    @Column(name = "triggered_rule_id", nullable = false)
    private UUID triggeredRuleId;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AlertSeverity severity;

    @NotBlank(message = "Alert message is required")
    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @NotNull(message = "Triggered time is required")
    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AlertStatus status = AlertStatus.NEW;

    @Column(name = "notes", length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
