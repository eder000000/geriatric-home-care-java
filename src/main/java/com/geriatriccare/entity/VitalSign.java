package com.geriatriccare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vital_signs", indexes = {
    @Index(name = "idx_vital_patient_measured", columnList = "patient_id, measured_at"),
    @Index(name = "idx_vital_measured_at", columnList = "measured_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VitalSign {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @NotNull(message = "Measurement time is required")
    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    // Blood Pressure (mmHg)
    @Min(value = 40, message = "Systolic BP must be at least 40")
    @Max(value = 250, message = "Systolic BP must not exceed 250")
    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;

    @Min(value = 20, message = "Diastolic BP must be at least 20")
    @Max(value = 150, message = "Diastolic BP must not exceed 150")
    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;

    // Heart Rate (bpm)
    @Min(value = 30, message = "Heart rate must be at least 30")
    @Max(value = 200, message = "Heart rate must not exceed 200")
    @Column(name = "heart_rate")
    private Integer heartRate;

    // Temperature (Celsius)
    @DecimalMin(value = "32.0", message = "Temperature must be at least 32°C")
    @DecimalMax(value = "45.0", message = "Temperature must not exceed 45°C")
    @Column(name = "temperature")
    private Double temperature;

    // Respiratory Rate (breaths per minute)
    @Min(value = 5, message = "Respiratory rate must be at least 5")
    @Max(value = 60, message = "Respiratory rate must not exceed 60")
    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    // Oxygen Saturation (%)
    @Min(value = 50, message = "Oxygen saturation must be at least 50")
    @Max(value = 100, message = "Oxygen saturation must not exceed 100")
    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation;

    // Context Information
    @Column(name = "position", length = 20)
    private String position; // SITTING, STANDING, LYING

    @Column(name = "measurement_method", length = 20)
    private String measurementMethod; // MANUAL, AUTOMATED

    @Column(name = "notes", length = 500)
    private String notes;

    // Audit Fields
    @Column(name = "recorded_by")
    private UUID recordedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
