package com.geriatriccare.entity;

import com.geriatriccare.enums.ReportFormat;
import com.geriatriccare.enums.ReportType;
import com.geriatriccare.enums.TimePeriod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MedicationAdherenceReport Entity
 * Tracks medication intake adherence for patients
 * Sprint 8 - US-7.2
 */
@Entity
@Table(name = "medication_adherence_reports", indexes = {
    @Index(name = "idx_med_report_patient", columnList = "patient_id"),
    @Index(name = "idx_med_report_medication", columnList = "medication_id"),
    @Index(name = "idx_med_report_generated", columnList = "generated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationAdherenceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "medication_id")
    private UUID medicationId; // Null = all medications

    @NotNull(message = "Report type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private ReportType reportType;

    @NotNull(message = "Time period is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "time_period", nullable = false, length = 20)
    private TimePeriod timePeriod;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // Metrics
    @Column(name = "total_scheduled_doses")
    private Integer totalScheduledDoses;

    @Column(name = "taken_doses")
    private Integer takenDoses;

    @Column(name = "missed_doses")
    private Integer missedDoses;

    @Column(name = "late_doses")
    private Integer lateDoses;

    @Column(name = "adherence_percentage")
    private Double adherencePercentage;

    @Column(name = "trend", length = 20)
    private String trend; // IMPROVING, DECLINING, STABLE

    @Column(name = "is_high_risk")
    private Boolean isHighRisk; // >30% missed

    // Pattern detection
    @Column(name = "weekend_adherence")
    private Double weekendAdherence;

    @Column(name = "weekday_adherence")
    private Double weekdayAdherence;

    @Column(name = "morning_adherence")
    private Double morningAdherence;

    @Column(name = "evening_adherence")
    private Double eveningAdherence;

    @Column(name = "most_missed_time", length = 50)
    private String mostMissedTime; // e.g., "Weekend evenings"

    // Report metadata
    @Column(name = "report_title", length = 255)
    private String reportTitle;

    @Column(name = "report_summary", length = 1000)
    private String reportSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 10)
    private ReportFormat format;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    // Audit
    @Column(name = "generated_by")
    private UUID generatedBy;

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public Double calculateAdherenceRate() {
        if (totalScheduledDoses == null || totalScheduledDoses == 0) {
            return 0.0;
        }
        return (takenDoses.doubleValue() / totalScheduledDoses.doubleValue()) * 100.0;
    }

    public boolean hasPatternDetected() {
        return weekendAdherence != null && weekdayAdherence != null &&
               Math.abs(weekendAdherence - weekdayAdherence) > 10.0;
    }
}
