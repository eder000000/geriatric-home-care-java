package com.geriatriccare.dto.medication;

import com.geriatriccare.enums.ReportFormat;
import com.geriatriccare.enums.ReportType;
import com.geriatriccare.enums.TimePeriod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationAdherenceReportRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private UUID medicationId; // Null = all medications

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    private TimePeriod timePeriod; // Default: LAST_30_DAYS

    private LocalDateTime startDate; // For CUSTOM period

    private LocalDateTime endDate; // For CUSTOM period

    private ReportFormat format; // Default: JSON

    private Boolean includePatterns; // Include pattern detection
}
