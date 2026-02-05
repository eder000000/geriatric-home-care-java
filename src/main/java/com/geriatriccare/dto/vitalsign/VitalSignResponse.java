package com.geriatriccare.dto.vitalsign;

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
public class VitalSignResponse {
    private UUID id;
    private UUID patientId;
    private LocalDateTime measuredAt;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Integer heartRate;
    private Double temperature;
    private Integer respiratoryRate;
    private Integer oxygenSaturation;
    private String position;
    private String measurementMethod;
    private String notes;
    private UUID recordedBy;
    private LocalDateTime createdAt;
}
