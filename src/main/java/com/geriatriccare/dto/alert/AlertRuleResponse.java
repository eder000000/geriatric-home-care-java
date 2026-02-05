package com.geriatriccare.dto.alert;

import com.geriatriccare.enums.AlertSeverity;
import com.geriatriccare.enums.ComparisonOperator;
import com.geriatriccare.enums.VitalSignType;
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
public class AlertRuleResponse {
    private UUID id;
    private UUID patientId;
    private VitalSignType vitalSignType;
    private AlertSeverity severity;
    private ComparisonOperator comparisonOperator;
    private Double thresholdValue;
    private Double thresholdValueMax;
    private String alertMessage;
    private Boolean isActive;
    private Integer cooldownMinutes;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
