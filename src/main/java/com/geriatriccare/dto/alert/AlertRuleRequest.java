package com.geriatriccare.dto.alert;

import com.geriatriccare.enums.AlertSeverity;
import com.geriatriccare.enums.ComparisonOperator;
import com.geriatriccare.enums.VitalSignType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleRequest {
    
    private UUID patientId; // null for global rule

    @NotNull(message = "Vital sign type is required")
    private VitalSignType vitalSignType;

    @NotNull(message = "Severity is required")
    private AlertSeverity severity;

    @NotNull(message = "Comparison operator is required")
    private ComparisonOperator comparisonOperator;

    @NotNull(message = "Threshold value is required")
    private Double thresholdValue;

    private Double thresholdValueMax; // For BETWEEN operator

    @NotBlank(message = "Alert message is required")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String alertMessage;

    private Integer cooldownMinutes;
}
