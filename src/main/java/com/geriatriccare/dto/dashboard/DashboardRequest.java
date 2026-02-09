package com.geriatriccare.dto.dashboard;

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
public class DashboardRequest {

    private TimePeriod timePeriod; // Default: LAST_30_DAYS

    private LocalDateTime startDate; // For CUSTOM period

    private LocalDateTime endDate; // For CUSTOM period

    private List<UUID> careTeamIds; // Filter by care team

    private String facilityFilter; // Filter by facility (future)

    private Boolean includeInactive; // Include inactive patients
}
