package com.geriatriccare.dto.medication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationAdherenceStatistics {

    private UUID patientId;
    private Integer totalMedications;
    private Integer activeMedications;
    private Integer totalScheduledDoses;
    private Integer takenDoses;
    private Integer missedDoses;
    private Integer lateDoses;
    private Double overallAdherence;
    private String trend;
    private Boolean atRisk; // >30% missed = high risk
    private Integer consecutiveMissedDays;
    private String primaryIssue; // e.g., "Weekend non-adherence"
}
