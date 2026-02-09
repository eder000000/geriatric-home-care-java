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
 * AdherenceReport Entity
 * Stores generated reports for care plan and medication adherence
 * Sprint 8 - US-7.1
 */
@Entity
@Table(name = "adherence_reports", indexes = {
    @Index(name = "idx_report_patient", columnList = "patient_id"),
    @Index(name = "idx_report_type", columnList = "report_type"),
    @Index(name = "idx_report_generated", columnList = "generated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdherenceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

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
    @Column(name = "total_tasks")
    private Integer totalTasks;

    @Column(name = "completed_tasks")
    private Integer completedTasks;

    @Column(name = "missed_tasks")
    private Integer missedTasks;

    @Column(name = "adherence_percentage")
    private Double adherencePercentage;

    @Column(name = "trend", length = 20)
    private String trend; // IMPROVING, DECLINING, STABLE

    // Report metadata
    @Column(name = "report_title", length = 255)
    private String reportTitle;

    @Column(name = "report_summary", length = 1000)
    private String reportSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 10)
    private ReportFormat format;

    @Column(name = "file_path", length = 500)
    private String filePath; // For PDF/CSV exports

    @Column(name = "file_size")
    private Long fileSize; // In bytes

    // Audit
    @Column(name = "generated_by")
    private UUID generatedBy;

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Auto-delete old reports

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public Double calculateAdherenceRate() {
        if (totalTasks == null || totalTasks == 0) {
            return 0.0;
        }
        return (completedTasks.doubleValue() / totalTasks.doubleValue()) * 100.0;
    }
}
