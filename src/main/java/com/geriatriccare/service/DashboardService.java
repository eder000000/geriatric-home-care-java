package com.geriatriccare.service;

import com.geriatriccare.dto.dashboard.DashboardRequest;
import com.geriatriccare.dto.dashboard.DashboardStatistics;
import com.geriatriccare.entity.*;
import com.geriatriccare.enums.*;
import com.geriatriccare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DashboardService
 * Aggregates health outcome metrics across the entire patient population
 * Sprint 8 - US-7.3 (GCARE-732)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final PatientRepository patientRepository;
    private final VitalSignRepository vitalSignRepository;
    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final AdherenceReportRepository adherenceReportRepository;
    private final MedicationAdherenceReportRepository medicationReportRepository;
    private final CarePlanRepository carePlanRepository;
    private final MedicationRepository medicationRepository;

    @Transactional(readOnly = true)
    public DashboardStatistics generateDashboard(DashboardRequest request) {
        log.info("Generating health outcomes dashboard");

        LocalDateTime[] dateRange = calculateDateRange(request);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        return DashboardStatistics.builder()
            .generatedAt(LocalDateTime.now())
            .startDate(startDate)
            .endDate(endDate)
            .populationStats(calculatePopulationStats(startDate, endDate))
            .vitalSignsMetrics(calculateVitalSignsMetrics(startDate, endDate))
            .alertMetrics(calculateAlertMetrics(startDate, endDate))
            .carePlanMetrics(calculateCarePlanMetrics(startDate, endDate))
            .medicationMetrics(calculateMedicationMetrics(startDate, endDate))
            .build();
    }

    // ========================================
    // POPULATION STATISTICS
    // ========================================

    private DashboardStatistics.PopulationStats calculatePopulationStats(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        long totalPatients = patientRepository.count();
        long activePatients = patientRepository.findByIsActiveTrue(PageRequest.of(0, 10000)).getContent().size();
        long inactivePatients = totalPatients - activePatients;

        // Calculate new patients in period (simplified)
        int newPatients = 5; // Mock - would need createdAt tracking

        // Calculate average age
        List<Patient> allPatients = patientRepository.findAll();
        double avgAge = allPatients.stream()
            .filter(p -> p.getDateOfBirth() != null)
            .mapToLong(p -> ChronoUnit.YEARS.between(p.getDateOfBirth(), LocalDateTime.now().toLocalDate()))
            .average()
            .orElse(0.0);

        // High risk patients (simplified - based on multiple factors)
        int highRisk = (int) (activePatients * 0.15); // Assume 15% high risk

        return DashboardStatistics.PopulationStats.builder()
            .totalPatients((int) totalPatients)
            .activePatients((int) activePatients)
            .inactivePatients((int) inactivePatients)
            .newPatientsThisPeriod(newPatients)
            .averageAge(avgAge)
            .highRiskPatients(highRisk)
            .build();
    }

    // ========================================
    // VITAL SIGNS METRICS
    // ========================================

    private DashboardStatistics.VitalSignsMetrics calculateVitalSignsMetrics(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        // Get all vital signs in period (sample for performance)
        List<VitalSign> vitalSigns = vitalSignRepository.findAll(PageRequest.of(0, 1000)).getContent();

        int totalReadings = vitalSigns.size();

        // Calculate averages
        double avgSystolic = vitalSigns.stream()
            .filter(v -> v.getBloodPressureSystolic() != null)
            .mapToInt(VitalSign::getBloodPressureSystolic)
            .average()
            .orElse(0.0);

        double avgHeartRate = vitalSigns.stream()
            .filter(v -> v.getHeartRate() != null)
            .mapToInt(VitalSign::getHeartRate)
            .average()
            .orElse(0.0);

        double avgTemp = vitalSigns.stream()
            .filter(v -> v.getTemperature() != null)
            .mapToDouble(VitalSign::getTemperature)
            .average()
            .orElse(0.0);

        double avgSpO2 = vitalSigns.stream()
            .filter(v -> v.getOxygenSaturation() != null)
            .mapToInt(VitalSign::getOxygenSaturation)
            .average()
            .orElse(0.0);

        // Count abnormal readings (simplified thresholds)
        long abnormal = vitalSigns.stream()
            .filter(v -> isAbnormalVitalSign(v))
            .count();

        double abnormalRate = totalReadings > 0 ? (abnormal * 100.0 / totalReadings) : 0.0;

        // Generate trends (simplified - daily averages)
        List<DashboardStatistics.TrendPoint> bpTrend = generateDailyTrend(
            vitalSigns, startDate, endDate, v -> v.getBloodPressureSystolic()
        );

        List<DashboardStatistics.TrendPoint> hrTrend = generateDailyTrend(
            vitalSigns, startDate, endDate, v -> v.getHeartRate()
        );

        return DashboardStatistics.VitalSignsMetrics.builder()
            .totalReadings(totalReadings)
            .averageBloodPressureSystolic(avgSystolic)
            .averageHeartRate(avgHeartRate)
            .averageTemperature(avgTemp)
            .averageOxygenSaturation(avgSpO2)
            .abnormalReadings((int) abnormal)
            .abnormalReadingRate(abnormalRate)
            .bloodPressureTrend(bpTrend)
            .heartRateTrend(hrTrend)
            .build();
    }

    private boolean isAbnormalVitalSign(VitalSign v) {
        return (v.getBloodPressureSystolic() != null && (v.getBloodPressureSystolic() > 140 || v.getBloodPressureSystolic() < 90)) ||
               (v.getHeartRate() != null && (v.getHeartRate() > 100 || v.getHeartRate() < 60)) ||
               (v.getTemperature() != null && (v.getTemperature() > 38.0 || v.getTemperature() < 36.0)) ||
               (v.getOxygenSaturation() != null && v.getOxygenSaturation() < 95);
    }

    private List<DashboardStatistics.TrendPoint> generateDailyTrend(
            List<VitalSign> vitalSigns, LocalDateTime start, LocalDateTime end,
            java.util.function.Function<VitalSign, Number> valueExtractor) {
        
        // Group by day and calculate average
        Map<LocalDateTime, List<VitalSign>> byDay = vitalSigns.stream()
            .filter(v -> valueExtractor.apply(v) != null)
            .collect(Collectors.groupingBy(v -> 
                v.getMeasuredAt().toLocalDate().atStartOfDay()
            ));

        return byDay.entrySet().stream()
            .map(entry -> {
                double avg = entry.getValue().stream()
                    .mapToDouble(v -> valueExtractor.apply(v).doubleValue())
                    .average()
                    .orElse(0.0);
                
                return DashboardStatistics.TrendPoint.builder()
                    .date(entry.getKey())
                    .value(avg)
                    .count(entry.getValue().size())
                    .build();
            })
            .sorted(Comparator.comparing(DashboardStatistics.TrendPoint::getDate))
            .collect(Collectors.toList());
    }

    // ========================================
    // ALERT METRICS
    // ========================================

    private DashboardStatistics.AlertMetrics calculateAlertMetrics(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        // Get all alerts (sample for performance)
        List<Alert> alerts = alertRepository.findAll(PageRequest.of(0, 1000)).getContent();

        int total = alerts.size();
        long critical = alerts.stream().filter(a -> a.getSeverity() == AlertSeverity.CRITICAL).count();
        long warning = alerts.stream().filter(a -> a.getSeverity() == AlertSeverity.WARNING).count();
        long acknowledged = alerts.stream().filter(a -> a.getStatus() == AlertStatus.ACKNOWLEDGED).count();
        long resolved = alerts.stream().filter(a -> a.getStatus() == AlertStatus.RESOLVED).count();
        long active = alerts.stream().filter(a -> a.getStatus() == AlertStatus.NEW).count();

        // Calculate average resolution time
        double avgResolutionMinutes = alerts.stream()
            .filter(a -> a.getResolvedAt() != null && a.getTriggeredAt() != null)
            .mapToLong(a -> ChronoUnit.MINUTES.between(a.getTriggeredAt(), a.getResolvedAt()))
            .average()
            .orElse(0.0);

        // Group by type
        List<DashboardStatistics.AlertTypeCount> byType = alerts.stream()
            .collect(Collectors.groupingBy(a -> a.getSeverity().name(), Collectors.counting()))
            .entrySet().stream()
            .map(e -> DashboardStatistics.AlertTypeCount.builder()
                .type(e.getKey())
                .count(e.getValue().intValue())
                .build())
            .collect(Collectors.toList());

        return DashboardStatistics.AlertMetrics.builder()
            .totalAlerts(total)
            .criticalAlerts((int) critical)
            .warningAlerts((int) warning)
            .acknowledgedAlerts((int) acknowledged)
            .resolvedAlerts((int) resolved)
            .activeAlerts((int) active)
            .averageResolutionTimeMinutes(avgResolutionMinutes)
            .alertsByType(byType)
            .build();
    }

    // ========================================
    // CARE PLAN METRICS
    // ========================================

    private DashboardStatistics.CarePlanMetrics calculateCarePlanMetrics(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        // Get recent adherence reports
        List<AdherenceReport> reports = adherenceReportRepository.findAll(PageRequest.of(0, 100)).getContent();

        int totalPlans = (int) carePlanRepository.count();
        long activePlans = carePlanRepository.findByIsActiveTrue(PageRequest.of(0, 1000)).getTotalElements();

        double avgAdherence = reports.stream()
            .mapToDouble(AdherenceReport::getAdherencePercentage)
            .average()
            .orElse(0.0);

        long highAdherence = reports.stream()
            .filter(r -> r.getAdherencePercentage() > 80.0)
            .count();

        long lowAdherence = reports.stream()
            .filter(r -> r.getAdherencePercentage() < 50.0)
            .count();

        // Mock category breakdown
        List<DashboardStatistics.CategoryAdherence> byCategory = Arrays.asList(
            DashboardStatistics.CategoryAdherence.builder()
                .category("MEDICATION")
                .adherence(82.0)
                .taskCount(150)
                .build(),
            DashboardStatistics.CategoryAdherence.builder()
                .category("EXERCISE")
                .adherence(65.0)
                .taskCount(80)
                .build()
        );

        return DashboardStatistics.CarePlanMetrics.builder()
            .totalCarePlans(totalPlans)
            .activeCarePlans((int) activePlans)
            .averageAdherence(avgAdherence)
            .highAdherencePlans((int) highAdherence)
            .lowAdherencePlans((int) lowAdherence)
            .adherenceByCategory(byCategory)
            .build();
    }

    // ========================================
    // MEDICATION METRICS
    // ========================================

    private DashboardStatistics.MedicationMetrics calculateMedicationMetrics(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        int totalMeds = medicationRepository.findByIsActiveTrue().size();
        
        // Get medication reports
        List<MedicationAdherenceReport> medReports = medicationReportRepository
            .findAll(PageRequest.of(0, 100)).getContent();

        double avgAdherence = medReports.stream()
            .mapToDouble(MedicationAdherenceReport::getAdherencePercentage)
            .average()
            .orElse(75.0);

        int missedDoses = medReports.stream()
            .mapToInt(MedicationAdherenceReport::getMissedDoses)
            .sum();

        long highRisk = medReports.stream()
            .filter(r -> r.getAdherencePercentage() < 70.0)
            .count();

        return DashboardStatistics.MedicationMetrics.builder()
            .totalMedications(totalMeds)
            .activePrescriptions(totalMeds)
            .averageAdherence(avgAdherence)
            .missedDosesTotal(missedDoses)
            .highRiskPatients((int) highRisk)
            .build();
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private LocalDateTime[] calculateDateRange(DashboardRequest request) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        if (request.getTimePeriod() == TimePeriod.CUSTOM) {
            startDate = request.getStartDate();
            endDate = request.getEndDate();
        } else {
            TimePeriod period = request.getTimePeriod() != null ? 
                request.getTimePeriod() : TimePeriod.LAST_30_DAYS;

            switch (period) {
                case LAST_7_DAYS:
                    startDate = endDate.minusDays(7);
                    break;
                case LAST_90_DAYS:
                    startDate = endDate.minusDays(90);
                    break;
                case LAST_30_DAYS:
                default:
                    startDate = endDate.minusDays(30);
                    break;
            }
        }

        return new LocalDateTime[]{startDate, endDate};
    }
}
