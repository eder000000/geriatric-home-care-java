package com.geriatriccare.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DashboardStatistics
 * Aggregate health outcome metrics for administrators
 * Sprint 8 - US-7.3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatistics {

    private LocalDateTime generatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Population Statistics
    private PopulationStats populationStats;

    // Vital Signs Metrics
    private VitalSignsMetrics vitalSignsMetrics;

    // Alert Metrics
    private AlertMetrics alertMetrics;

    // Care Plan Metrics
    private CarePlanMetrics carePlanMetrics;

    // Medication Metrics
    private MedicationMetrics medicationMetrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PopulationStats {
        private Integer totalPatients;
        private Integer activePatients;
        private Integer inactivePatients;
        private Integer newPatientsThisPeriod;
        private Double averageAge;
        private Integer highRiskPatients; // Based on adherence/vitals
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VitalSignsMetrics {
        private Integer totalReadings;
        private Double averageBloodPressureSystolic;
        private Double averageHeartRate;
        private Double averageTemperature;
        private Double averageOxygenSaturation;
        private Integer abnormalReadings;
        private Double abnormalReadingRate;
        private List<TrendPoint> bloodPressureTrend;
        private List<TrendPoint> heartRateTrend;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertMetrics {
        private Integer totalAlerts;
        private Integer criticalAlerts;
        private Integer warningAlerts;
        private Integer acknowledgedAlerts;
        private Integer resolvedAlerts;
        private Integer activeAlerts;
        private Double averageResolutionTimeMinutes;
        private List<AlertTypeCount> alertsByType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CarePlanMetrics {
        private Integer totalCarePlans;
        private Integer activeCarePlans;
        private Double averageAdherence;
        private Integer highAdherencePlans; // >80%
        private Integer lowAdherencePlans; // <50%
        private List<CategoryAdherence> adherenceByCategory;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicationMetrics {
        private Integer totalMedications;
        private Integer activePrescriptions;
        private Double averageAdherence;
        private Integer missedDosesTotal;
        private Integer highRiskPatients; // <70% adherence
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        private LocalDateTime date;
        private Double value;
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertTypeCount {
        private String type;
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryAdherence {
        private String category;
        private Double adherence;
        private Integer taskCount;
    }
}
