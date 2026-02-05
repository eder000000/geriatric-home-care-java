package com.geriatriccare.dto.alert;

import com.geriatriccare.enums.AlertSeverity;
import com.geriatriccare.enums.AlertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {
    private UUID id;
    private UUID patientId;
    private UUID vitalSignId;
    private UUID triggeredRuleId;
    private AlertSeverity severity;
    private String message;
    private LocalDateTime triggeredAt;
    private LocalDateTime acknowledgedAt;
    private UUID acknowledgedBy;
    private LocalDateTime resolvedAt;
    private UUID resolvedBy;
    private AlertStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
