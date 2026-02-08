package com.geriatriccare.dto.medication;

import com.geriatriccare.enums.ReportFormat;
import com.geriatriccare.enums.ReportType;
import com.geriatriccare.enums.TimePeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationAdherenceReportResponse {

    private UUID reportId;
    private UUID patientId;
    private String patientName;
    private UUID medicationId;
    private String medicationName;
    private ReportType reportType;
    private TimePeriod timePeriod;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Metrics
    private Integer totalScheduledDoses;
    private Integer takenDoses;
    private Integer missedDoses;
    private Integer lateDoses;
    private Double adherencePercentage;
    private String trend;
    private Boolean isHighRisk;

    // Patterns
    private AdherencePattern patterns;

    // Breakdown by medication
    private List<MedicationBreakdown> medicationBreakdown;

    // Time series
    private List<DailyAdherence> dailyAdherence;

    // Report metadata
    private String reportTitle;
    private String reportSummary;
    private ReportFormat format;
    private String downloadUrl;
    private UUID generatedBy;
    private LocalDateTime generatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdherencePattern {
        private Double weekendAdherence;
        private Double weekdayAdherence;
        private Double morningAdherence;
        private Double eveningAdherence;
        private String mostMissedTime;
        private String insight; // e.g., "Patient struggles with weekend doses"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicationBreakdown {
        private UUID medicationId;
        private String medicationName;
        private Integer scheduled;
        private Integer taken;
        private Integer missed;
        private Double adherenceRate;
        private Boolean isCritical; // Critical medication flag
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyAdherence {
        private LocalDateTime date;
        private Integer scheduled;
        private Integer taken;
        private Integer missed;
        private Double rate;
    }
}
