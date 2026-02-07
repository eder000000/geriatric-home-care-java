package com.geriatriccare.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdherenceStatistics {

    private UUID patientId;
    private Integer totalCarePlans;
    private Integer activeCarePlans;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer missedTasks;
    private Integer pendingTasks;
    private Double overallAdherence;
    private Double averageDailyAdherence;
    private String trend;
    private Integer consecutiveMissedDays;
    private Boolean atRisk; // >30% missed = at risk
}
