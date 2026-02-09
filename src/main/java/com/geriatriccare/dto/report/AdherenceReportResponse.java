package com.geriatriccare.dto.report;

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
public class AdherenceReportResponse {

    private UUID reportId;
    private UUID patientId;
    private String patientName;
    private ReportType reportType;
    private TimePeriod timePeriod;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Metrics
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer missedTasks;
    private Integer pendingTasks;
    private Double adherencePercentage;
    private String trend; // IMPROVING, DECLINING, STABLE

    // Breakdown by category
    private List<CategoryAdherence> categoryBreakdown;

    // Time series data
    private List<DailyAdherence> dailyAdherence;

    // Report metadata
    private String reportTitle;
    private String reportSummary;
    private ReportFormat format;
    private String downloadUrl; // For PDF/CSV
    private UUID generatedBy;
    private LocalDateTime generatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryAdherence {
        private String category;
        private Integer total;
        private Integer completed;
        private Double adherenceRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyAdherence {
        private LocalDateTime date;
        private Integer completed;
        private Integer missed;
        private Double rate;
    }
}
