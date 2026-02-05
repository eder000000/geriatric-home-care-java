package com.geriatriccare.dto.vitalsign;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private LocalDateTime measuredAt; // If null, use current time

    @Min(value = 40, message = "Systolic BP must be at least 40")
    @Max(value = 250, message = "Systolic BP must not exceed 250")
    private Integer bloodPressureSystolic;

    @Min(value = 20, message = "Diastolic BP must be at least 20")
    @Max(value = 150, message = "Diastolic BP must not exceed 150")
    private Integer bloodPressureDiastolic;

    @Min(value = 30, message = "Heart rate must be at least 30")
    @Max(value = 200, message = "Heart rate must not exceed 200")
    private Integer heartRate;

    @DecimalMin(value = "32.0", message = "Temperature must be at least 32°C")
    @DecimalMax(value = "45.0", message = "Temperature must not exceed 45°C")
    private Double temperature;

    @Min(value = 5, message = "Respiratory rate must be at least 5")
    @Max(value = 60, message = "Respiratory rate must not exceed 60")
    private Integer respiratoryRate;

    @Min(value = 50, message = "Oxygen saturation must be at least 50")
    @Max(value = 100, message = "Oxygen saturation must not exceed 100")
    private Integer oxygenSaturation;

    private String position; // SITTING, STANDING, LYING
    private String measurementMethod; // MANUAL, AUTOMATED

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
